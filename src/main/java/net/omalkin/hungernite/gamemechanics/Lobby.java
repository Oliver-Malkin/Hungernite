package net.omalkin.hungernite.gamemechanics;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Lobby {
    private final String id;
    private UUID owner; // Lobbies can be transferred between players
    private final Set<UUID> players = new HashSet<>(); // Set of players

    public Lobby(String id, ServerPlayer owner){
        this.owner = Objects.requireNonNull(owner.getUUID());
        this.players.add(owner.getUUID());
        this.id = Objects.requireNonNull(id);
    }

    public void removePlayer(ServerPlayer player){
        players.remove(player.getUUID());
    }
    public void addPlayer(ServerPlayer player){
        players.add(player.getUUID());
    }
    public UUID getOwner() {
        return this.owner;
    }
    public String getId() {
        return this.id;
    }
    public Set<UUID> getPlayers() {
        return this.players;
    }
    public void setOwner(ServerPlayer owner) {
        this.owner = owner.getUUID();
    }
    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}
