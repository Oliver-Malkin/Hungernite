package net.omalkin.hungernite.network.handle;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.omalkin.hungernite.network.packets.SetupScreen;

public class HandleSetupScreen {
    public static void handle(SetupScreen data, IPayloadContext context) {
        // Must not run the open screen on the server. This prevents classloading
        context.enqueueWork(Guard::openScreen)
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("networking.hungernite.failed", e.getMessage()));
                    return null;
                });
    }

    static class Guard {
        static void openScreen(){
            Minecraft.getInstance().setScreen(new net.omalkin.hungernite.screen.custom.SetupScreen());
        }
    }
}
