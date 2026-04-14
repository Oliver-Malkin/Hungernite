package net.omalkin.hungernite.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.omalkin.hungernite.Hungernite;
import net.omalkin.hungernite.network.handle.SetupScreenHandle;
import net.omalkin.hungernite.network.handle.UpdateGameModeHandle;
import net.omalkin.hungernite.network.handle.UpdateMapTypeHandle;
import net.omalkin.hungernite.network.handle.UpdateStartingKitHandle;
import net.omalkin.hungernite.network.packets.SetupScreenPacket;
import net.omalkin.hungernite.network.packets.UpdateGameModePacket;
import net.omalkin.hungernite.network.packets.UpdateMapTypePacket;
import net.omalkin.hungernite.network.packets.UpdateStartingKitPacket;

@EventBusSubscriber(modid = Hungernite.MODID)
public class ModPackets {
    private static final String VERSION = "0"; // Change this when making API breaking changes

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(VERSION);
        // can add .executesOn(HandlerThread.NETWORK); //so the process runs on the networking thread
        registrar.playToClient(
                SetupScreenPacket.TYPE,
                SetupScreenPacket.STREAM_CODEC,
                SetupScreenHandle::handle
        );

        registrar.playToServer(
                UpdateMapTypePacket.TYPE,
                UpdateMapTypePacket.STREAM_CODEC,
                UpdateMapTypeHandle::handle
        );

        registrar.playToServer(
                UpdateStartingKitPacket.TYPE,
                UpdateStartingKitPacket.STREAM_CODEC,
                UpdateStartingKitHandle::handle
        );

        registrar.playToServer(
                UpdateGameModePacket.TYPE,
                UpdateGameModePacket.STREAM_CODEC,
                UpdateGameModeHandle::handle
        );
    }
}
