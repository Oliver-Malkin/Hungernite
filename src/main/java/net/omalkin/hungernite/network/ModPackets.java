package net.omalkin.hungernite.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.omalkin.hungernite.Hungernite;
import net.omalkin.hungernite.network.handle.HandleSetupScreen;
import net.omalkin.hungernite.network.packets.SetupScreen;

@EventBusSubscriber(modid = Hungernite.MODID)
public class ModPackets {
    private static final String VERSION = "0"; // Change this when making API breaking changes

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar(VERSION);
            // can add .executesOn(HandlerThread.NETWORK); //so the process runs on the networking thread
        registrar.playToClient(
                SetupScreen.TYPE,
                SetupScreen.STREAM_CODEC,
                HandleSetupScreen::handle
        );
    }
}
