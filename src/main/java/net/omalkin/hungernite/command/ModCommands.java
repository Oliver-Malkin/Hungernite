package net.omalkin.hungernite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.omalkin.hungernite.Hungernite;
import net.omalkin.hungernite.gamemechanics.Lobby;
import net.omalkin.hungernite.gamemechanics.LobbyManager;

@EventBusSubscriber(modid = Hungernite.MODID)
public class ModCommands {
    // Registration
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Lobby admin commands
        // These can only be run by a lobby admin. An admin is automatically created when using the /hnnewlobby
        dispatcher.register(
                Commands.literal("hnstart").executes(ModCommands::startCommand)
        );

        dispatcher.register(
                Commands.literal("hnstop").executes(ModCommands::stopCommand)
        );

        dispatcher.register(
                Commands.literal("hnpause").executes(ModCommands::pauseCommand)
        );

        dispatcher.register(
                Commands.literal("hnunpause").executes(ModCommands::unpauseCommand)
        );

        dispatcher.register(
                Commands.literal("hndisband").executes(ModCommands::disbandCommand)
        );

        dispatcher.register(
                Commands.literal("hnsetup").executes(ModCommands::setupCommand)
        );

        dispatcher.register(
                Commands.literal("hntransfer")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ModCommands::transferCommand)
                        )
        );


        // Standard user commands
        dispatcher.register(
                Commands.literal("hnnewlobby").executes(ModCommands::newLobbyCommand)
        );

        dispatcher.register(
                Commands.literal("hnjoin")
                        .then(Commands.argument("lobbyId", StringArgumentType.string())
                                .executes(ModCommands::joinCommand)
                        )
        );

        dispatcher.register(
                Commands.literal("hnleave").executes(ModCommands::leaveCommand)
        );


        // Server admin commands OP level 4
        dispatcher.register(
                Commands.literal("hnlist")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .executes(ModCommands::listCommand)
        );

        dispatcher.register(
                Commands.literal("hnterminate")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(Commands.argument("lobbyId", StringArgumentType.string())  // Always need an ID
                            .executes(ModCommands::terminateCommand)
                                .then(Commands.argument("reason", StringArgumentType.string())  // Can provide a reason
                                        .executes(ModCommands::terminateWithReasonCommand))
                        )
        );

        dispatcher.register(
                Commands.literal("hnstats")
                        .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                        .then(Commands.argument("lobbyId", StringArgumentType.string())
                                .executes(ModCommands::statsCommand)
                        )
        );

    }

    // Command execution methods
    private static int statsCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "lobbyId");
        Lobby lobby = LobbyManager.getLobby(lobbyId);
        String message = "Owner: " + lobby.getOwner().toString() + "\nPlayers: " + lobby.getPlayers().toString();
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal(message), true);
        return 1;
    }

    private static int terminateWithReasonCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "lobbyId");
        String reason = StringArgumentType.getString(commandSourceStackCommandContext, "reason");
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Terminated lobby: " + lobbyId + " for: " + reason), true);
        return 1;
    }

    private static int terminateCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "lobbyId");
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Terminated lobby: " + lobbyId), true);
        return 1;
    }

    private static int listCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbies = LobbyManager.getLobbies().toString();
        commandSourceStackCommandContext.getSource().sendSuccess(() ->Component.literal("Lobbies: " + lobbies), false);
        return 1;
    }

    private static int leaveCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        try {
            String result = LobbyManager.leaveLobby(commandSourceStackCommandContext.getSource().getPlayer());
            commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal(result), false);
        } catch (IllegalStateException e) {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 1;
    }

    private static int joinCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "lobbyId");
        ServerPlayer player = commandSourceStackCommandContext.getSource().getPlayer();
        try {
            LobbyManager.joinLobby(player, lobbyId);
            commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("You have joined the game: " + lobbyId), false);
        } catch (IllegalStateException e) {
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 1;
    }

    private static int newLobbyCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        try {
            String lobbyId = LobbyManager.createLobby(commandSourceStackCommandContext.getSource().getPlayer());
            commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Lobby: " + lobbyId + " created!"), false);
        } catch (IllegalStateException e){
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 1;
    }

    private static int transferCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) throws CommandSyntaxException {
        ServerPlayer newPlayer = EntityArgument.getPlayer(commandSourceStackCommandContext, "target");
        try {
            LobbyManager.transfer(commandSourceStackCommandContext.getSource().getPlayer(), newPlayer);
            commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Lobby transferred to " + newPlayer.getName().getString()), false);
        } catch (IllegalStateException e){
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 1;
    }

    private static int setupCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Setup"), false);
        return 1;
    }

    private static int disbandCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        try {
            LobbyManager.disband(commandSourceStackCommandContext.getSource().getPlayer());
            commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Lobby disbanded"), false);
        } catch (IllegalStateException e){
            commandSourceStackCommandContext.getSource().sendFailure(Component.literal(e.getMessage()));
        }
        return 1;
    }

    private static int unpauseCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Unpause"), false);
        return 1;
    }

    private static int pauseCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Pause"), false);
        return 1;
    }

    private static int stopCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Stop"), false);
        return 1;
    }

    private static int startCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Start").withColor(ChatFormatting.AQUA.getColor()), false);
        return 1;
    }
}
