package net.omalkin.hungernite.gamemechanics;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.UsernameCache;

import java.util.*;

public class LobbyManager {
    private static final Map<String, Lobby> lobbies = new HashMap<>(); // Stores lobbyId, lobbyInstance
    private static final Map<UUID, String> playerToLobby = new HashMap<>(); // Stores UUID, lobbyId (reverse lookup)

    // Used to generate the random IDs
    private static final String LETTER_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ1246789";
    private static final Random RANDOM = new Random();
    private static final int LOBBY_CODE_LENGTH = 5;

    public static String createLobby(ServerPlayer owner){
        if(playerToLobby.containsKey(owner.getUUID())){
            throw new IllegalStateException("You are already in a lobby");
        }

        String id = createLobbyId();
        Lobby lobby = new Lobby(id, owner);
        lobbies.put(id, lobby);
        playerToLobby.put(owner.getUUID(), id);

        return id;
    }

    public static void joinLobby(ServerPlayer player, String lobbyId){
        if(lobbies.containsKey(lobbyId)) {
            if(playerToLobby.containsKey(player.getUUID())){
                if(lobbies.get(lobbyId).getPlayers().contains(player.getUUID())){
                    throw new IllegalStateException("You are already in this lobby");
                }
                throw new IllegalStateException("You are already in a lobby");
            } else{
                // Add the player to the lobby
                lobbies.get(lobbyId).addPlayer(player);
                playerToLobby.put(player.getUUID(), lobbyId);
            }
        } else {
            throw new IllegalStateException("The lobby " + lobbyId + " does not exist");
        }
    }

    public static String leaveLobby(ServerPlayer player){
        UUID playerId = player.getUUID();

        if(playerToLobby.containsKey(playerId)){ // Is the player in a lobby
            String lobbyId = playerToLobby.get(playerId);
            Lobby lobby = lobbies.get(lobbyId);

            if(lobby.getOwner() == playerId){ // Owner leaving
                if(lobby.getPlayers().size() == 1) { // Last man standing, destroy lobby
                    lobbies.remove(lobbyId);
                    playerToLobby.remove(playerId);

                    return "You have left the lobby\nNo players left in lobby. " + lobbyId + " has been destroyed";
                } else{ // Transfer lobby to someone else
                    // Remove owner
                    lobby.removePlayer(player);
                    playerToLobby.remove(playerId);

                    // Transfer to new player
                    UUID newPlayer = lobby.getPlayers().iterator().next();
                    lobby.setOwner(newPlayer);

                    return "You have left the lobby\nLobby ownership transferred to: " + UsernameCache.getLastKnownUsername(newPlayer);
                }
            } else {
                lobbies.get(lobbyId).removePlayer(player);
                playerToLobby.remove(playerId);
                lobby.removePlayer(player);

                return "You have left the lobby";
            }
        } else {
            throw new IllegalStateException("You are not in a lobby");
        }
    }

    // This destroys the lobby
    public static boolean disband(ServerPlayer player) {
        Lobby lobby = getLobby(player);

        if (playerToLobby.containsKey(player.getUUID())){
            if (isOwner(player)) { // This player owns this lobby
                // Delete players from reverse lookup
                for (UUID p : lobby.getPlayers()) {
                    playerToLobby.remove(p);
                }
                lobbies.remove(lobby.getId()); // Delete the lobby reference

                return true;
            } else {
                throw new IllegalStateException("You are not the lobby owner");
            }
        } else {
            throw new IllegalStateException("You are not in a lobby");
        }
    }

    public static void transfer(ServerPlayer currentPlayer, ServerPlayer newPlayer) {
        if (playerToLobby.containsKey(currentPlayer.getUUID())){
            if (isOwner(currentPlayer)) {
                if (getLobby(currentPlayer).getPlayers().contains(newPlayer.getUUID())) { // The new player is in the current lobby
                    getLobby(currentPlayer).setOwner(newPlayer);
                } else {
                    throw new IllegalStateException(newPlayer.getName().getString() + " is not in the current lobby");
                }
            } else {
                throw new IllegalStateException("You are not the lobby owner");
            }
        } else {
            throw new IllegalStateException("You are not in a lobby");
        }
    }

    public static Map<String, Lobby> getLobbies(){
        return new HashMap<>(lobbies);
    }

    public static Lobby getLobby(String id){
        return lobbies.get(id);
    }

    public static Lobby getLobby(ServerPlayer player){
        return lobbies.get(playerToLobby.get(player.getUUID()));
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
        } while (lobbies.containsKey(stringBuilder.toString()));

        return stringBuilder.toString();
    }

    private static boolean isOwner(ServerPlayer player){
        return getLobby(player).getOwner() == player.getUUID();
    }
}
