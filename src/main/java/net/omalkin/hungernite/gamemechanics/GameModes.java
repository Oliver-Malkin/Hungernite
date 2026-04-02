package net.omalkin.hungernite.gamemechanics;

import net.minecraft.network.chat.Component;

public enum GameModes {
    NO_CRAFTING("gamemode.no_crafting", "gamemod.no_crafting.desc"),
    TEAMS("gamemode.teams", "gamemod.teams.desc"),
    HIDE_AND_SEEK("gamemode.hide_and_seek", "gamemod.hide_and_seek.desc"),
    REDUCED_VIS("gamemode.rediced_vis", "gamemod.rediced_vis.desc"),
    PACKED_TOGETHER("gamemode.packed_together", "gamemod.packed_together.desc");

    private final String nameKey;
    private final String descKey;

    GameModes(String nameKey, String descKey) {
        this.nameKey = nameKey;
        this.descKey = descKey;
    }

    public Component getName(){
        return Component.translatable(nameKey);
    }

    public Component getDescKey() {
        return Component.translatable(descKey);
    }
}
