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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LobbyManager {
    private static final Map<String, Lobby> LOBBIES = new HashMap<>(); // Stores lobbyId, lobbyInstance
    private static final Map<UUID, String> PLAYER_TO_LOBBY = new HashMap<>(); // Stores UUID, lobbyId (reverse lookup)

    // Used to generate the random IDs
    private static final String LETTER_POOL = "ABCDEFGHJKMNPQRSTUVWXYZ1246789";
    private static final Random RANDOM = new Random();
    private static final int LOBBY_CODE_LENGTH = 4;

    // USER COMMANDS
    public static int create(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            failure(context, Component.translatable("message.hungernite.lobby.already_in", getLobby(context.getSource().getPlayer()).getId()));
        } else {
            String id = createLobbyId();
            Lobby lobby = new Lobby(id, player);
            LOBBIES.put(id, lobby);
            PLAYER_TO_LOBBY.put(player.getUUID(), id);

            success(context, Component.translatable("message.hungernite.lobby.created", id), false);
        }

        return 1;
    }

    public static int join(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        String lobbyId = StringArgumentType.getString(context, "lobbyId").toUpperCase();

        if (isPlayerInLobby(player)) {
            failure(context, Component.translatable("message.hungernite.lobby.already_in", PLAYER_TO_LOBBY.get(player.getUUID())));
        } else {
            if (LOBBIES.containsKey(lobbyId)) {
                Lobby lobby = LOBBIES.get(lobbyId);
                if (lobby.isKicked(player)) {
                    failure(context, Component.translatable("message.hungernite.lobby.join_failure", lobbyId)
                            .append(Component.literal(". "))
                            .append(Component.translatable("message.hungernite.lobby.kicked")));
                } else {
                    if (lobby.isRunning()) {
                        failure(context, Component.translatable("message.hungernite.lobby.join_failure", lobbyId)
                                .append(Component.literal(". "))
                                .append(Component.translatable("message.hungernite.game.already_running")));
                    } else {
                        lobby.addPlayer(player);
                        PLAYER_TO_LOBBY.put(player.getUUID(), lobbyId);
                        success(context, Component.translatable("message.hungernite.lobby.join_success", lobbyId), false);
                    }
                }
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.non_existant", lobbyId));
            }
        }

        return 1;
    }

    public static int leave(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        UUID playerId = player.getUUID();

        if (isPlayerInLobby(player)) { // Is the player in a lobby
            Lobby lobby = getLobby(player);
            String lobbyId = lobby.getId();
            if (isOwner(player)) { // Owner leaving
                if (lobby.getPlayers().size() == 1) { // Last man standing, destroy lobby
                    LOBBIES.remove(lobbyId);
                    PLAYER_TO_LOBBY.remove(playerId);
                    success(context, Component.translatable("message.hungernite.lobby.leave_success")
                            .append(Component.literal("\n"))
                            .append(Component.translatable("message.hungernite.lobby.no_players_left", lobbyId))
                            .append(Component.literal(". "))
                            .append(Component.translatable("message.hungernite.lobby.disbanded")), false);
                } else { // Transfer lobby to someone else
                    // Remove owner
                    lobby.removePlayer(player);
                    PLAYER_TO_LOBBY.remove(playerId);

                    // Transfer to new player
                    UUID newPlayer = lobby.getPlayers().iterator().next();
                    lobby.setOwner(newPlayer);
                    success(context, Component.translatable("message.hungernite.lobby.leave_success")
                            .append(Component.literal("\n"))
                            .append(Component.translatable("message.hungernite.lobby.transfer",
                                    UsernameCache.getLastKnownUsername(newPlayer))), false);
                }
            } else {
                LOBBIES.get(lobbyId).removePlayer(player);
                PLAYER_TO_LOBBY.remove(playerId);
                lobby.removePlayer(player);
                success(context, Component.translatable("message.hungernite.lobby.leave_success"), false);
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    // LOBBY ADMIN COMMANDS
    // This destroys the lobby
    public static int disband(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        Lobby lobby = getLobby(player);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) { // This player owns this lobby
                destroyLobby(lobby);
                success(context, Component.translatable("message.hungernite.lobby.disbanded"), false);
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int setup(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) {
                PacketDistributor.sendToPlayer(player, new SetupScreen());
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int transfer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer currentPlayer = getServerPlayerFromContext(context);
        ServerPlayer newPlayer = EntityArgument.getPlayer(context, "target");
        if (isPlayerInLobby(currentPlayer)) {
            if (isOwner(currentPlayer)) {
                if (getLobby(currentPlayer).getPlayers().contains(newPlayer.getUUID())) { // The new player is in the current lobby
                    getLobby(currentPlayer).setOwner(newPlayer);
                    success(context, Component.translatable("message.hungernite.lobby.transfer",
                            newPlayer.getName().getString()), false);
                } else {
                    failure(context, Component.translatable("message.hungernite.lobby.player_not_in", newPlayer.getName().getString()));
                }
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int kick(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer currentPlayer = getServerPlayerFromContext(context);
        ServerPlayer kickedPlayer = EntityArgument.getPlayer(context, "target");
        if (isPlayerInLobby(currentPlayer)) {
            if (isOwner(currentPlayer)) {
                if (isPlayerInLobby(kickedPlayer)) {
                    if (currentPlayer == kickedPlayer) {
                        failure(context, Component.translatable("message.hungernite.lobby.kick_self"));
                    } else {
                        getLobby(currentPlayer).kickPlayer(kickedPlayer);
                        PLAYER_TO_LOBBY.remove(kickedPlayer.getUUID());
                        Component kickMessage = Component.translatable("message.hungernite.lobby.kick", kickedPlayer.getName());
                        try {
                            String reason = StringArgumentType.getString(context, "reason");
                            kickMessage = kickMessage.copy()
                                    .append(Component.literal(". "))
                                    .append(Component.translatable("message.hungernite.lobby.reason", reason));
                        } catch (IllegalArgumentException ignored) {
                        }
                        success(context, kickMessage, true);
                    }
                } else {
                    failure(context, Component.translatable("message.hungernite.lobby.player_not_in", kickedPlayer.getName().getString()));
                }
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int start(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) {
                Lobby lobby = getLobby(player);
                lobby.setGameState(GameStates.IN_PROGRESS);
                success(context, Component.translatable("message.hungernite.game.start"), false);
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int stop(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) {
                Lobby lobby = getLobby(player);
                lobby.setGameState(GameStates.IN_LOBBY);
                success(context, Component.translatable("message.hungernite.game.stop"), false);
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int pause(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) {
                Lobby lobby = getLobby(player);
                if (lobby.isRunning()) {
                    if (lobby.isPaused()) {
                        failure(context, Component.translatable("message.hungernite.game.already_paused"));
                    } else {
                        lobby.setGameState(GameStates.PAUSED);
                        success(context, Component.translatable("message.hungernite.game.pause"), false);
                    }
                } else {
                    failure(context, Component.translatable("message.hungernite.game.not_running"));
                }
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    public static int unpause(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = getServerPlayerFromContext(context);
        if (isPlayerInLobby(player)) {
            if (isOwner(player)) {
                Lobby lobby = getLobby(player);
                if (lobby.isRunning()) {
                    if (!lobby.isPaused()) {
                        failure(context, Component.translatable("message.hungernite.game.already_running"));
                    } else {
                        lobby.setGameState(GameStates.IN_PROGRESS);
                        success(context, Component.translatable("message.hungernite.game.unpause"), false);
                    }
                } else {
                    failure(context, Component.translatable("message.hungernite.game.already_running"));
                }
            } else {
                failure(context, Component.translatable("message.hungernite.lobby.not_owner"));
            }
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.not_in"));
        }

        return 1;
    }

    // SERVER ADMIN COMMANDS
    // This can only be triggered from a OP level 4 and from the server CLI
    public static int terminate(CommandContext<CommandSourceStack> context) {
        String lobbyId = StringArgumentType.getString(context, "lobbyId").toUpperCase();
        String reason = StringArgumentType.getString(context, "reason");
        if (LOBBIES.containsKey(lobbyId)) {
            destroyLobby(LOBBIES.get(lobbyId));
            success(context, Component.translatable("message.hungernite.lobby.terminate", lobbyId)
                    .append(Component.translatable("message.hungernite.lobby.reason", reason)), true);
        } else {
            failure(context, Component.translatable("message.hungernite.lobby.non_existant", lobbyId));
        }

        return 1;
    }


    // Can make these private eventually
    public static Map<String, Lobby> getLobbies() {
        return new HashMap<>(LOBBIES);
    }

    public static Lobby getLobby(String id) {
        return LOBBIES.get(id);
    }

    public static Lobby getLobby(ServerPlayer player) {
        return LOBBIES.get(PLAYER_TO_LOBBY.get(player.getUUID()));
    }

    // Helpers
    private static String createLobbyId() {
        StringBuilder stringBuilder = new StringBuilder();

        do {
            stringBuilder.setLength(0);
            for (int i = 0; i < LOBBY_CODE_LENGTH; i++) {
                char letter = LETTER_POOL.charAt(RANDOM.nextInt(LETTER_POOL.length()));
                stringBuilder.append(letter);
            }
        } while (LOBBIES.containsKey(stringBuilder.toString()));

        return stringBuilder.toString();
    }

    private static boolean isOwner(ServerPlayer player) {
        return getLobby(player).getOwner() == player.getUUID();
    }

    private static boolean isPlayerInLobby(ServerPlayer player) {
        return PLAYER_TO_LOBBY.containsKey(player.getUUID());
    }

    private static void destroyLobby(Lobby lobby) {
        // Delete players from reverse lookup
        for (UUID p : lobby.getPlayers()) {
            PLAYER_TO_LOBBY.remove(p);
        }
        LOBBIES.remove(lobby.getId()); // Delete the lobby reference
    }

    private static void success(CommandContext<CommandSourceStack> context, Component message, boolean logging) {
        context.getSource().sendSuccess(() -> message, logging);
    }

    private static void failure(CommandContext<CommandSourceStack> context, Component message) {
        context.getSource().sendFailure(message);
    }

    private static ServerPlayer getServerPlayerFromContext(CommandContext<CommandSourceStack> context) {
        return context.getSource().getPlayer();
    }
}
