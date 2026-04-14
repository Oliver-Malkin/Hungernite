package net.omalkin.hungernite.network.handle;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.omalkin.hungernite.network.packets.SetupScreenPacket;
import net.omalkin.hungernite.screen.custom.SetupScreen;

// Client handler
public class SetupScreenHandle {
    public static void handle(SetupScreenPacket data, IPayloadContext context) {
        // Must not run the open screen on the server. This prevents classloading
        context.enqueueWork(() -> Guard.openScreen(data))
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("networking.hungernite.failed", e.getMessage()));
                    return null;
                });
    }

    static class Guard {
        static void openScreen(SetupScreenPacket data) {
            Minecraft.getInstance().setScreen(new SetupScreen(data.lobbyId(), data.gameModes(), data.mapType(), data.startingKits()));
        }
    }
}
