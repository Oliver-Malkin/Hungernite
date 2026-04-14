package net.omalkin.hungernite.network.handle;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.omalkin.hungernite.gamemechanics.GameModes;
import net.omalkin.hungernite.gamemechanics.GenerationOptions;
import net.omalkin.hungernite.gamemechanics.LobbyManager;
import net.omalkin.hungernite.network.packets.UpdateGameModePacket;
import net.omalkin.hungernite.network.packets.UpdateMapTypePacket;

// Server handler
public class UpdateGameModeHandle {
    public static void handle(final UpdateGameModePacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
                    LobbyManager.updateGameMode(GameModes.valueOf(data.gameMode()), data.state(), ((ServerPlayer) context.player()));
                })
                .exceptionally(e -> {
                    context.disconnect(Component.translatable("networking.hungernite.failed", e.getMessage()));
                    return null;
                });
    }
}
