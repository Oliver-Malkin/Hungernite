package net.omalkin.hungernite.network.handle;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.omalkin.hungernite.gamemechanics.GenerationOptions;
import net.omalkin.hungernite.gamemechanics.LobbyManager;
import net.omalkin.hungernite.network.packets.SetupScreenPacket;
import net.omalkin.hungernite.network.packets.UpdateMapTypePacket;
import net.omalkin.hungernite.screen.custom.SetupScreen;

// Server handler
public class UpdateMapTypeHandle {
    public static void handle(final UpdateMapTypePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
                    LobbyManager.updateMapType(GenerationOptions.valueOf(data.mapType()), (ServerPlayer) context.player());
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("networking.hungernite.failed", e.getMessage()));
                    return null;
                });
    }

    static class Guard {
        static void openScreen(){
            Minecraft.getInstance().setScreen(new SetupScreen());
        }
    }
}
