package net.omalkin.hungernite.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.omalkin.hungernite.Hungernite;

public record UpdateGameModePacket(String gameMode, boolean state) implements CustomPacketPayload {
    public static final Type<UpdateGameModePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hungernite.MODID, "update_game_mode"));

    public static final StreamCodec<ByteBuf, UpdateGameModePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpdateGameModePacket::gameMode,
            ByteBufCodecs.BOOL,
            UpdateGameModePacket::state,
            UpdateGameModePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
