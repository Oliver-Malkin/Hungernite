package net.omalkin.hungernite.gamemechanics;

import net.minecraft.network.chat.Component;

// Types of maps
public enum GenerationOptions {
    DEFAULT("generation.hungernite.default", "generation.hungernite.default.desc"),
    SKY_ISLANDS("generation.hungernite.sky_islands", "generation.hungernite.sky_islands.desc"),
    CONTINENTS("generation.hungernite.continents", "generation.hungernite.continents.desc"),
    BIG_ISLANDS("generation.hungernite.big_islands", "generation.hungernite.big_islands.desc"),
    UNDERGROUND_TUNNELS("generation.hungernite.tunnels", "generation.hungernite.tunnels.desc"),
    SPRING("generation.hungernite.spring", "generation.hungernite.spring.desc"),
    SUMMER("generation.hungernite.summer", "generation.hungernite.summer.desc"),
    AUTUMN("generation.hungernite.autumn", "generation.hungernite.autumn.desc"),
    WINTER("generation.hungernite.winter", "generation.hungernite.winter.desc");

    private final String nameKey;
    private final String descKey;

    GenerationOptions(String nameKey, String descKey) {
        this.nameKey = nameKey;
        this.descKey = descKey;
    }

    public Component getName() {
        return Component.translatable(nameKey);
    }

    public Component getDescKey() {
        return Component.translatable(descKey);
    }

    @Override
    public String toString() {
        return getName().getString();
    }
}
