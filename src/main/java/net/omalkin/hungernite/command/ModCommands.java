package net.omalkin.hungernite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.UsernameCache;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.omalkin.hungernite.Hungernite;
import net.omalkin.hungernite.gamemechanics.Lobby;
import net.omalkin.hungernite.gamemechanics.LobbyManager;

import java.util.UUID;

@EventBusSubscriber(modid = Hungernite.MODID)
public class ModCommands {
    // Registration
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Lobby admin commands
        // These can only be run by a lobby admin. An admin is automatically created when using the /hnnewlobby
        dispatcher.register(
                Commands.literal("hnstart")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::start)
        );

        dispatcher.register(
                Commands.literal("hnstop")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::stop)
        );

        dispatcher.register(
                Commands.literal("hnpause")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::pause)
        );

        dispatcher.register(
                Commands.literal("hnunpause")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::unpause)
        );

        dispatcher.register(
                Commands.literal("hndisband")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::disband)
        );

        dispatcher.register(
                Commands.literal("hnsetup")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::setup)
        );

        dispatcher.register(
                Commands.literal("hntransfer")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(LobbyManager::transfer)
                        )
        );

        dispatcher.register(
                Commands.literal("hnkick")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(LobbyManager::kick)
                                    .then(Commands.argument("reason", StringArgumentType.greedyString())
                                            .executes(LobbyManager::kick)
                                    )
                        )
        );


        // Standard user commands
        dispatcher.register(
                Commands.literal("hnnewlobby")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::create)
        );

        dispatcher.register(
                Commands.literal("hnjoin")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .then(Commands.argument("lobbyId", StringArgumentType.greedyString())
                                .executes(LobbyManager::join)
                        )
        );

        dispatcher.register(
                Commands.literal("hnleave")
                        .requires(commandSourceStack -> commandSourceStack.getEntity() instanceof ServerPlayer)
                        .executes(LobbyManager::leave)
        );


        // Server admin commands OP level 4. Can be run from server CLI
        dispatcher.register(
                Commands.literal("hnlist")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(ModCommands::listCommand)
        );

        dispatcher.register(
                Commands.literal("hnterminate")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(Commands.argument("lobbyId", StringArgumentType.string())  // Always need an ID
                        .then(Commands.argument("reason", StringArgumentType.greedyString())  // Must also provide a reason
                            .executes(LobbyManager::terminate))
                        )
        );

        dispatcher.register(
                Commands.literal("hnstats")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(Commands.argument("lobbyId", StringArgumentType.greedyString())
                                .executes(ModCommands::statsCommand)
                        )
        );
    }

    private static int statsCommand(CommandContext<CommandSourceStack> context) {
        String lobbyId = StringArgumentType.getString(context, "lobbyId").toUpperCase();
        Lobby lobby = LobbyManager.getLobby(lobbyId);
        StringBuilder players = new StringBuilder();
        for (UUID p : lobby.getPlayers()){
            players.append(UsernameCache.getLastKnownUsername(p));
            players.append(" ");
        }
        String message = "Owner: " + UsernameCache.getLastKnownUsername(lobby.getOwner()) + "\nPlayers: " + players;
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int listCommand(CommandContext<CommandSourceStack> context) {
        String lobbies = LobbyManager.getLobbies().toString();
        context.getSource().sendSuccess(() ->Component.literal("Lobbies: " + lobbies), false);
        return 1;
    }

    private static int setupCommand(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Setup"), false);
        return 1;
    }
}
