package net.omalkin.hungernite.gamemechanics;

import net.minecraft.network.chat.Component;

public enum GameModes {
    NO_CRAFTING("gamemode.hungernite.no_crafting", "gamemode.hungernite.no_crafting.desc"),
    TEAMS("gamemode.hungernite.teams", "gamemode.hungernite.teams.desc"),
    HIDE_AND_SEEK("gamemode.hungernite.hide_and_seek", "gamemode.hungernite.hide_and_seek.desc"),
    REDUCED_VIS("gamemode.hungernite.rediced_vis", "gamemode.hungernite.rediced_vis.desc"),
    PACKED_TOGETHER("gamemode.hungernite.packed_together", "gamemode.hungernite.packed_together.desc"),
    FAST_PACED("gamemode.hungernite.fast_paced", "gamemode.hungernite.fast_paced.desc");

    private final String nameKey;
    private final String descKey;

    GameModes(String nameKey, String descKey) {
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
        return this.getName().getString();
    }
}
