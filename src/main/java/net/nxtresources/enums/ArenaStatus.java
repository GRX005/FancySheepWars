package net.nxtresources.enums;

public enum ArenaStatus {
    WAITING("§eVárakozás"), STARTED("§aJátékban");

    final String statusName;
    ArenaStatus(String statusName){
        this.statusName =statusName;

    }
    public static String getFormattedName(ArenaStatus status) {
        return status.statusName;
    }


}
