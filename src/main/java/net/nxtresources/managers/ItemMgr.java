package net.nxtresources.managers;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemMgr {

    public static void lobbyItems(Player player){
        ItemStack arenaselector = new ItemBuilder(Material.BOOK)
                .setDisplayName("§eAréna választó")
                .setAmount(1)
                .build();
        player.getInventory().setItem(4, arenaselector);

    }

    //setup items
    public static ItemStack blue = new ItemBuilder(Material.BLUE_WOOL)
            .setDisplayName("§9§lKÉK §fcsapat")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel kattintva elkezded setupolni az adott csapatot"))
            .build();
    public static ItemStack red = new ItemBuilder(Material.RED_WOOL)
            .setDisplayName("§c§lPIROS §fcsapat")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel kattintva elkezded setupolni az adott csapatot"))
            .build();
    public static ItemStack leaveSetup = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§cSetup mód elhagyása")
            .build();
    public static ItemStack saveAndExit = new ItemBuilder(Material.EMERALD_BLOCK)
            .setDisplayName("§aMentés és kilépés a setup módból")
            .build();
    public static ItemStack setwaitinglobby = new ItemBuilder(Material.DARK_OAK_DOOR)
            .setDisplayName("§aVárakozó lobby beállítása")
            .setLore(
                    LegacyComponentSerializer.legacySection().deserialize("§eVálassz ki egy várakozó lobbynak megfelelő helyet,"),
                    LegacyComponentSerializer.legacySection().deserialize("§eállj oda majd kattints ezzel az itemmel!"))
            .build();
    public static ItemStack selectorTool =new ItemBuilder(Material.WOODEN_AXE)
            .setDisplayName("§AKijelölő")
            .build();
    public static ItemStack setsheep=new ItemBuilder(Material.ORANGE_WOOL)
            .setDisplayName("bb")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel állíthatod be a bárányokat hogy hol spawnoljanak tetszőleges mennyiségben."))
            .build();
    public static ItemStack waitingSelectorTool=new ItemBuilder(Material.GOLDEN_AXE)
            .setDisplayName("WaitingLobby kijelolo")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel kell kijelolni a waiting lobby területét hogy aréna közben eltünjön."))
            .build();

    //default items
    public static ItemStack leaveArena = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§eAréna elhagyása")
            .build();

    //sheeps
    public static ItemStack explSheep = new ItemBuilder(Material.PLAYER_HEAD)
            .setDisplayName("§aExplosive Sheep")
            .setPD("expl")
            .setSkin("ewogICJ0aW1lc3RhbXAiIDogMTcwOTE2NTY1MjEwNywKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjNjZjZmQ5ZjNhNzY1YThjZTUwZDdkNDEzZGZjMjRmMjg2NTU2YzJiNmFkNTA5Njg5OGNmYTkzODBjMzkxNjkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==","q7WLkV0hjPTeKSU5hCQV3y38EBZGL6T2c/mN7ugqo21+wylRGUHPv1If9ubgDZpGYc7hYfoa/TCcPCMkuWZ2Df3o8TIUFmj+vlnduOyrOekITr9crqjioGu6O3DDngB719XYzDeVzrr5420oBPe5TiuqAg3osUjppDr0eCJUXCen69decD9hErmKJvsQn/XCkVOQZSA0h+hgj9zViJxOo57ITmwuV29Ha2zr4g0bNlEoCr9wP/mrA1yWBWvqFmcd1Yh2EWd/22uMSjsbwt7vUnltqyU7++mS6PXcBNsKgE18zUlA2aP/4J+//6g0rxQNjnG8samC75RI5qyIAX4C2el4od7iH/EoPmbnR0BchRzdSVOU19nVhfRyFpzY55vUznmX/FG7Tn+VqO1qhjyfc3ZOto9v5lM5SXGzb1FR1j3j9m+Fef71dYPNZn5bSOve6K0wi1jmuZYL4B1HMiDApckWQXlV7NQPAumrz7PsLaoiE8F01GJUr7/AW8Zeh7Kk5uh4zoJoczYQ7eR1cjgwErUJR3GlwerB2fHB9zO7ffDWwlsqWJcuFvaNzOHuzdItOJ22ULqD22HYJG4vO5gQzvXqg/FkzBTnaQ0k8HUAonpwO97FihG5WLZuA1/uH7RKljMrVosaUV5Kgtd9UIYSa8fUjxVWcOPWTESuFkVgdm0=")
            .build();

}
