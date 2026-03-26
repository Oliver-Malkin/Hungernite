package net.omalkin.hungernite.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.omalkin.hungernite.Hungernite;

import java.util.Objects;

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
                Commands.literal("hntransfer").executes(ModCommands::transferCommand)
        );


        // Standard user commands
        dispatcher.register(
                Commands.literal("hnnewlobby").executes(ModCommands::lobbyCommand)
        );

        dispatcher.register(
                Commands.literal("hnjoin")
                        .then(Commands.argument("id", StringArgumentType.string())
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
                        .then(Commands.argument("id", StringArgumentType.string())  // Always need an ID
                            .executes(ModCommands::terminateCommand)
                                .then(Commands.argument("reason", StringArgumentType.string())  // Can provide a reason
                                        .executes(ModCommands::terminateWithReasonCommand))
                        )
        );

    }

    // Command execution methods
    private static int terminateWithReasonCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "id");
        String reason = StringArgumentType.getString(commandSourceStackCommandContext, "reason");
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Terminated id: " + lobbyId + " for: " + reason), true);
        return 1;
    }

    private static int terminateCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String lobbyId = StringArgumentType.getString(commandSourceStackCommandContext, "id");
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal("Terminated id: " + lobbyId), true);
        return 1;
    }

    private static int listCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("List"), false);
        return 1;
    }

    private static int leaveCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Leave"), false);
        return 1;
    }

    private static int joinCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        String id = StringArgumentType.getString(commandSourceStackCommandContext, "id");
        String player = commandSourceStackCommandContext.getSource().getTextName();
        commandSourceStackCommandContext.getSource().sendSuccess(() -> Component.literal(player + " has joined game: " + id), false);
        return 1;
    }

    private static int lobbyCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Lobby"), false);
        return 1;
    }

    private static int transferCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Transfer"), false);
        return 1;
    }

    private static int setupCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Setup"), false);
        return 1;
    }

    private static int disbandCommand(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
        commandSourceStackCommandContext.getSource().getPlayer().displayClientMessage(Component.literal("Disband"), false);
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
