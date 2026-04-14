package net.omalkin.hungernite.network.handle;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.omalkin.hungernite.gamemechanics.GenerationOptions;
import net.omalkin.hungernite.gamemechanics.LobbyManager;
import net.omalkin.hungernite.gamemechanics.StartingKits;
import net.omalkin.hungernite.network.packets.UpdateMapTypePacket;
import net.omalkin.hungernite.network.packets.UpdateStartingKitPacket;

// Server handler
public class UpdateStartingKitHandle {
    public static void handle(final UpdateStartingKitPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
                    LobbyManager.updateKit(StartingKits.valueOf(data.kit()), (ServerPlayer) context.player());
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("networking.hungernite.failed", e.getMessage()));
                    return null;
                });
    }
}
