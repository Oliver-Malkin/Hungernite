package net.omalkin.hungernite.screen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.omalkin.hungernite.Hungernite;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Hungernite.MODID);

    public static void Register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
