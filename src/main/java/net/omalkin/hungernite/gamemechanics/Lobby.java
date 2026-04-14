package net.omalkin.hungernite.gamemechanics;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class Lobby {
    private final String id;
    private final Set<UUID> players = new HashSet<>(); // Set of players
    private final Set<UUID> kickedPlayers = new HashSet<>(); // Set of kicked players
    private final EnumSet<GameModes> gameModes;

    private UUID owner; // Lobbies can be transferred between players
    private GameStates gameState;
    private GenerationOptions generationOptions;
    private StartingKits startingKits;

    public Lobby(String id, ServerPlayer owner) {
        this.owner = Objects.requireNonNull(owner.getUUID());
        this.players.add(owner.getUUID());
        this.id = Objects.requireNonNull(id);
        this.gameState = GameStates.IN_LOBBY;
        this.gameModes = EnumSet.noneOf(GameModes.class);
        this.generationOptions = GenerationOptions.DEFAULT;
        this.startingKits = StartingKits.OFF;
    }

    // Player is moved from the players set to the kickedPlayers set
    public void kickPlayer(ServerPlayer player) {
        UUID playerId = player.getUUID();
        this.players.remove(playerId);
        this.kickedPlayers.add(playerId);
    }

    // Lobby questions
    public boolean isKicked(ServerPlayer player) {
        return this.kickedPlayers.contains(player.getUUID());
    }

    public boolean isEnabled(GameModes mode) {
        return gameModes.contains(mode);
    }

    public boolean isPaused() {
        return gameState == GameStates.PAUSED;
    }

    public boolean isRunning() {
        return gameState != GameStates.IN_LOBBY; // Any state other than IN_LOBBY is counted as running (paused is still running)
    }

    public void enable(GameModes mode) {
        gameModes.add(mode);
    }

    public void disable(GameModes mode) {
        gameModes.remove(mode);
    }

    public void removePlayer(ServerPlayer player) {
        this.players.remove(player.getUUID());
    }

    public void addPlayer(ServerPlayer player) {
        this.players.add(player.getUUID());
    }

    // Getters
    public UUID getOwner() {
        return this.owner;
    }

    public String getId() {
        return this.id;
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public EnumSet<GameModes> getGameModes() {
        return this.gameModes.clone();
    }

    public GameStates getGameState() {
        return this.gameState;
    }

    public GenerationOptions getGenerationOptions() {
        return this.generationOptions;
    }

    public StartingKits getStartingKit() {
        return this.startingKits;
    }

    // Setters
    public void setOwner(ServerPlayer owner) {
        this.owner = owner.getUUID();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setGameState(GameStates state) {
        this.gameState = state;
    }

    public void setGenerationOptions(GenerationOptions generationOptions) {
        this.generationOptions = generationOptions;
    }

    public void setStartingKit(StartingKits kit) {
        this.startingKits = kit;
    }
}
