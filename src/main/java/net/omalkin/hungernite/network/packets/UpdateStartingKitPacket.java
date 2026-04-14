package net.omalkin.hungernite.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.omalkin.hungernite.Hungernite;

public record UpdateStartingKitPacket(String kit) implements CustomPacketPayload {
    public static final Type<UpdateStartingKitPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hungernite.MODID, "update_starting_kit"));

    public static final StreamCodec<ByteBuf, UpdateStartingKitPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpdateStartingKitPacket::kit,
            UpdateStartingKitPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
