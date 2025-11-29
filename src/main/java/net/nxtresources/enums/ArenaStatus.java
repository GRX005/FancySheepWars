package net.nxtresources.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.managers.MsgCache;

public enum ArenaStatus {
    WAITING("Arena.WAITING"), STARTING("Arena.STARTING"), STARTED("Arena.STARTED");

    final String statusName;
    ArenaStatus(String statusName){
        this.statusName =statusName;

    }
    public String getConfig() {
        return statusName;
    }

    public static Component getFormattedStatus(ArenaStatus stat) {
        String raw = MsgCache.get(stat.getConfig());
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }
}
