package net.omalkin.hungernite.gamemechanics;

import net.minecraft.network.chat.Component;

public enum StartingKits {
    ON("screen.hungernite.on"),
    OFF("screen.hungernite.off"),
    RANDOM("screen.hungernite.random");

    private final String nameKey;

    StartingKits(String nameKey) {
        this.nameKey = nameKey;
    }

    public Component getName() {
        return Component.translatable(nameKey);
    }

    @Override
    public String toString() {
        return this.getName().getString();
    }
}
