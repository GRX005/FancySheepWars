package net.nxtresources.enums;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.DyeColor;

public enum SheepType {
    EXPLOSIVE(DyeColor.RED,  Component.text("Explosive Sheep", NamedTextColor.RED)),
    HEALING(DyeColor.PINK, Component.text("Healing Sheep", NamedTextColor.LIGHT_PURPLE));

    private final DyeColor dyeColor;
    private final Component displayName;

    SheepType(DyeColor dyeColor, Component displayName) {
        this.dyeColor = dyeColor;
        this.displayName = displayName;
    }

    public DyeColor dyeColor() {
        return dyeColor;
    }

    public Component displayName() {
        return displayName;
    }
}
