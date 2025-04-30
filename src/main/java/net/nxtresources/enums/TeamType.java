package net.nxtresources.enums;

public enum TeamType {
    BLUE("§9Kék"),RED("§cPiros");

    final String teamName;
    TeamType(String teamName){
        this.teamName =teamName;

    }
    public static String getFormattedName(TeamType type) {
        return type.teamName;
    }

}
