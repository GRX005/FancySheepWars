package net.nxtresources.enums;

public enum SetupStep {

    MAP_REGION, LOBBY_REGION, LOBBY_SPAWN, RED_TEAM_SPAWN, BLUE_TEAM_SPAWN, SHEEP_SPAWNS;

    public SetupStep next(){
        var ord = ordinal();
        return ord + 1 < values().length ? values()[ord + 1] : null;
    }
}
