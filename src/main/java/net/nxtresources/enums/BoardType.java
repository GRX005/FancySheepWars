package net.nxtresources.enums;

public enum BoardType {

    LOBBY("Lobby"), WAITING("Waiting"), INGAME("Ingame"), SETUP("Setup");

    final String value;
    final String prefix = "Scoreboards.";

    BoardType(String suffix){
        this.value = prefix + suffix;
    }

    public String get(){
        return value;
    }
}
