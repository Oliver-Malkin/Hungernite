package net.omalkin.hungernite.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.omalkin.hungernite.Hungernite;

public record SetupScreenPacket(String lobbyId, int gameModes, String mapType, String startingKits) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetupScreenPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Hungernite.MODID, "open_screen"));

    // List the data types here as pairs
    public static final StreamCodec<ByteBuf, SetupScreenPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SetupScreenPacket::lobbyId,
            ByteBufCodecs.INT,
            SetupScreenPacket::gameModes,
            ByteBufCodecs.STRING_UTF8,
            SetupScreenPacket::mapType,
            ByteBufCodecs.STRING_UTF8,
            SetupScreenPacket::startingKits,
            SetupScreenPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
