package net.nxtresources.enums;

public enum BoardType {

    LOBBYBOARD("Lobby"), WAITINGBOARD("Waiting"), INGAMEBOARD("Ingame"), SETUPBOARD("Setup");

    final String value;
    final String prefix = "Scoreboards.";

    BoardType(String suffix){
        this.value = prefix + suffix;
    }

    public String get(){
        return value;
    }
}
