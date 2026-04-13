package net.omalkin.hungernite.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.omalkin.hungernite.Hungernite;

public record UpdateMapTypePacket(String mapType) implements CustomPacketPayload {
    public static final Type<UpdateMapTypePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hungernite.MODID, "update_generation_settings"));

    // List the data types here as pairs
    public static final StreamCodec<ByteBuf, UpdateMapTypePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpdateMapTypePacket::mapType,
            UpdateMapTypePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
