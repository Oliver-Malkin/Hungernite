package net.omalkin.hungernite;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.omalkin.hungernite.screen.SetupScreen;
import net.omalkin.hungernite.util.KeyBindings;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Hungernite.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically start all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Hungernite.MODID, value = Dist.CLIENT)
public class HungerniteClient {
    public HungerniteClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Hungernite.LOGGER.info("HELLO FROM CLIENT SETUP");
        Hungernite.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.SETUP_KEYBIND);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if(KeyBindings.SETUP_KEYBIND.consumeClick()) {
            Minecraft.getInstance().setScreen(new SetupScreen());
        }
    }
}
