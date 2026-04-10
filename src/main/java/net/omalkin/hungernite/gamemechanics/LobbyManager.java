package net.omalkin.hungernite.gamemechanics;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.network.PacketDistributor;
import net.omalkin.hungernite.network.packets.SetupScreen;

import java.util.*;

public class LobbyManager {
    private static final Map<String, Lobby> LOBBIES = new HashMap<>(); // Stores lobbyId, lobbyInstance
    private static final Map<UUID, String> PLAYER_TO_LOBBY = new HashMap<>(); // Stores UUID, lobbyId (reverse lookup)

    // Used to generate the random IDs
    private static final String LETTER_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ1246789";
    private static final Random RANDOM = new Random();
    private static final int LOBBY_CODE_LENGTH = 4;

    // TODO: change string messages to components for translations

    // USER COMMANDS
    public static int create(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)){
            failure(context, "You are already in a lobby");
        } else{
            String id = createLobbyId();
            Lobby lobby = new Lobby(id, player);
            LOBBIES.put(id, lobby);
            PLAYER_TO_LOBBY.put(player.getUUID(), id);

            success(context, "Lobby: " + id + " created!", false);
        }

        return 1;
    }

    public static int join(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        String lobbyId = StringArgumentType.getString(context, "lobbyId").toUpperCase();
        if(LOBBIES.containsKey(lobbyId)) {
            Lobby lobby = LOBBIES.get(lobbyId);
            if(isPlayerInLobby(player)){
                if(lobby.getPlayers().contains(player.getUUID())){
                    failure(context, "You are already in this lobby");
                } else{
                    failure(context, "You are already in a lobby");
                }
            } else{
                if(lobby.isKicked(player)) {
                    failure(context, "You cannot join as you were kicked");
                } else {
                    if(lobby.isRunning()) {
                        failure(context, "The game is currently in progress");
                    } else {
                        lobby.addPlayer(player);
                        PLAYER_TO_LOBBY.put(player.getUUID(), lobbyId);
                        success(context, "You have joined the game: " + lobbyId, false);
                    }
                }
            }
        } else {
            failure(context, "The lobby " + lobbyId + " does not exist");
        }

        return 1;
    }

    public static int leave(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        UUID playerId = player.getUUID();
        if(PLAYER_TO_LOBBY.containsKey(playerId)){ // Is the player in a lobby
            String lobbyId = PLAYER_TO_LOBBY.get(playerId);
            Lobby lobby = LOBBIES.get(lobbyId);
            if(lobby.getOwner() == playerId){ // Owner leaving
                if(lobby.getPlayers().size() == 1) { // Last man standing, destroy lobby
                    LOBBIES.remove(lobbyId);
                    PLAYER_TO_LOBBY.remove(playerId);
                    success(context, "You have left the lobby\nNo players left in lobby. " + lobbyId + " has been destroyed", false);
                } else{ // Transfer lobby to someone else
                    // Remove owner
                    lobby.removePlayer(player);
                    PLAYER_TO_LOBBY.remove(playerId);

                    // Transfer to new player
                    UUID newPlayer = lobby.getPlayers().iterator().next();
                    lobby.setOwner(newPlayer);
                    success(context,"You have left the lobby\nLobby ownership transferred to: " + UsernameCache.getLastKnownUsername(newPlayer), false);
                }
            } else {
                LOBBIES.get(lobbyId).removePlayer(player);
                PLAYER_TO_LOBBY.remove(playerId);
                lobby.removePlayer(player);
                success(context,"You have left the lobby", false);
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    // LOBBY ADMIN COMMANDS
    // This destroys the lobby
    public static int disband(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        Lobby lobby = getLobby(player);
        if (isPlayerInLobby(player)){
            if (isOwner(player)) { // This player owns this lobby
                destroyLobby(lobby);
                success(context, "Lobby disbanded", false);
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int setup(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)){
            if(isOwner(player)){
                success(context, "Enter lobby setup", false);
                PacketDistributor.sendToPlayer(player, new SetupScreen());
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int transfer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer currentPlayer = getServerPlayerFromContext(context);
        ServerPlayer newPlayer = EntityArgument.getPlayer(context, "target");
        if (isPlayerInLobby(currentPlayer)){
            if (isOwner(currentPlayer)) {
                if (getLobby(currentPlayer).getPlayers().contains(newPlayer.getUUID())) { // The new player is in the current lobby
                    getLobby(currentPlayer).setOwner(newPlayer);
                    success(context,"Lobby transferred to " + newPlayer.getName().getString(), false);
                } else {
                    failure(context, newPlayer.getName().getString() + " is not in the current lobby");
                }
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int kick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer currentPlayer = getServerPlayerFromContext(context);
        ServerPlayer kickedPlayer = EntityArgument.getPlayer(context, "target");
        if(isPlayerInLobby(currentPlayer)){
            if(isOwner(currentPlayer)) {
                if(isPlayerInLobby(kickedPlayer)){
                    if(currentPlayer == kickedPlayer) {
                        failure(context, "You cannot kick yourself, that'll hurt");
                    } else {
                        getLobby(currentPlayer).kickPlayer(kickedPlayer);
                        PLAYER_TO_LOBBY.remove(kickedPlayer.getUUID());
                        try{
                            String reason = StringArgumentType.getString(context, "reason");
                            success(context, "Kicked player :" + kickedPlayer.getName() + ". Reason: " + reason, true);
                        } catch (IllegalArgumentException e){
                            success(context, "Kicked player :" + kickedPlayer.getName(), true);
                        }
                    }
                } else {
                    failure(context, kickedPlayer.getName().getString() + " is not in your lobby");
                }
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    // TODO: start and stop commands are temporary for now. They dont have much functionality
    public static int start(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)) {
            if(isOwner(player)) {
                Lobby lobby = getLobby(player);
                lobby.setGameState(GameStates.IN_PROGRESS);
                success(context, "Started the game!", false);
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int stop(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)) {
            if(isOwner(player)) {
                Lobby lobby = getLobby(player);
                lobby.setGameState(GameStates.IN_LOBBY);
                success(context, "Stopped the game!", false);
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int pause(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)){
            if(isOwner(player)) {
                Lobby lobby = getLobby(player);
                if(lobby.isRunning()) {
                    if(lobby.isPaused()){
                        failure(context, "Game already paused");
                    } else {
                        lobby.setGameState(GameStates.PAUSED);
                        success(context, "Game paused", false);
                    }
                } else {
                    failure(context, "Game is not running");
                }
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    public static int unpause(CommandContext<CommandSourceStack> context){
        ServerPlayer player = getServerPlayerFromContext(context);
        if(isPlayerInLobby(player)){
            if(isOwner(player)) {
                Lobby lobby = getLobby(player);
                if(lobby.isRunning()) {
                    if(!lobby.isPaused()){
                        failure(context, "Game already running");
                    } else {
                        lobby.setGameState(GameStates.IN_PROGRESS);
                        success(context, "Game unpaused", false);
                    }
                } else {
                    failure(context, "Game is not running");
                }
            } else {
                failure(context, "You are not the lobby owner");
            }
        } else {
            failure(context, "You are not in a lobby");
        }

        return 1;
    }

    // SERVER ADMIN COMMANDS
    // This can only be triggered from a OP level 4 and from the server CLI
    public static int terminate(CommandContext<CommandSourceStack> context){
        String lobbyId = StringArgumentType.getString(context, "lobbyId").toUpperCase();
        String reason = StringArgumentType.getString(context, "reason");
        if(LOBBIES.containsKey(lobbyId)){
            destroyLobby(LOBBIES.get(lobbyId));
            success(context, "Terminated lobby: " + lobbyId + ". Reason: " + reason, true);
        } else {
            failure(context, "Lobby does not exist");
        }

        return 1;
    }


    // Can make these private eventually
    public static Map<String, Lobby> getLobbies(){
        return new HashMap<>(LOBBIES);
    }
    public static Lobby getLobby(String id){
        return LOBBIES.get(id);
    }
    public static Lobby getLobby(ServerPlayer player){
        return LOBBIES.get(PLAYER_TO_LOBBY.get(player.getUUID()));
    }

    // Helpers
    private static String createLobbyId() {
        StringBuilder stringBuilder = new StringBuilder();

        do {
            stringBuilder.setLength(0);
            for(int i = 0; i < LOBBY_CODE_LENGTH; i++){
                char letter = LETTER_POOL.charAt(RANDOM.nextInt(LETTER_POOL.length()));
                stringBuilder.append(letter);
            }
        } while(LOBBIES.containsKey(stringBuilder.toString()));

        return stringBuilder.toString();
    }

    private static boolean isOwner(ServerPlayer player){
        return getLobby(player).getOwner() == player.getUUID();
    }
    private static boolean isPlayerInLobby(ServerPlayer player){
        return PLAYER_TO_LOBBY.containsKey(player.getUUID());
    }

    private static void destroyLobby(Lobby lobby) {
        // Delete players from reverse lookup
        for(UUID p : lobby.getPlayers()) {
            PLAYER_TO_LOBBY.remove(p);
        }
        LOBBIES.remove(lobby.getId()); // Delete the lobby reference
    }

    private static void success(CommandContext<CommandSourceStack> context, String message, boolean logging) {
        context.getSource().sendSuccess(() -> Component.literal(message), logging);
    }

    private static void failure(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(message));
    }

    private static ServerPlayer getServerPlayerFromContext(CommandContext<CommandSourceStack> context) {
        return context.getSource().getPlayer();
    }
}
