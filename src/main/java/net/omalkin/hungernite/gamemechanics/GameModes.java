package net.omalkin.hungernite.gamemechanics;

import net.minecraft.network.chat.Component;

public enum GameModes {
    NO_CRAFTING("gamemode.hungernite.no_crafting", "gamemod.hungernite.no_crafting.desc"),
    TEAMS("gamemode.hungernite.teams", "gamemod.hungernite.teams.desc"),
    HIDE_AND_SEEK("gamemode.hungernite.hide_and_seek", "gamemod.hungernite.hide_and_seek.desc"),
    REDUCED_VIS("gamemode.hungernite.rediced_vis", "gamemod.hungernite.rediced_vis.desc"),
    PACKED_TOGETHER("gamemode.hungernite.packed_together", "gamemod.hungernite.packed_together.desc");

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
