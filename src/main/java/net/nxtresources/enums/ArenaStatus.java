package net.nxtresources.enums;

public enum ArenaStatus {
    WAITING("Arena.WAITING"), STARTING("Arena.STARTING"), STARTED("Arena.STARTED");

    final String statusName;
    ArenaStatus(String statusName){
        this.statusName =statusName;

    }
    public String getConfig() {
        return statusName;
    }
}
