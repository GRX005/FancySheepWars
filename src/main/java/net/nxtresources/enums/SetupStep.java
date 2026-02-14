package net.nxtresources.enums;

public enum SetupStep {

    MAP_REGION("Arena.Setup.Steps.MapRegion"),
    LOBBY_REGION("Arena.Setup.Steps.LobbyRegion"),
    LOBBY_SPAWN("Arena.Setup.Steps.LobbySpawn"),
    RED_TEAM_SPAWNS("Arena.Setup.Steps.RedTeamSpawn"),
    BLUE_TEAM_SPAWNS("Arena.Setup.Steps.BlueTeamSpawn"),
    SHEEP_SPAWNS("Arena.Setup.Steps.SheepsSpawns");

    final String nextName;

    SetupStep(String nextName){
        this.nextName = nextName;
    }

    public String getStepName(){
        return nextName;
    }

    public SetupStep next(){
        var ord = ordinal();
        return ord + 1 < values().length ? values()[ord + 1] : null;
    }
}
