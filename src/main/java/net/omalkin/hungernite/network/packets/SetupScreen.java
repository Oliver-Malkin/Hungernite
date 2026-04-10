package net.omalkin.hungernite.network.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.omalkin.hungernite.Hungernite;

public record SetupScreen() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SetupScreen> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Hungernite.MODID, "open_screen"));

    // List the data types here as pairs
    public static final StreamCodec<ByteBuf, SetupScreen> STREAM_CODEC = StreamCodec.unit(new SetupScreen());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
