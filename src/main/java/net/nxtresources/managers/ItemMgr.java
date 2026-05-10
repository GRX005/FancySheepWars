package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import net.nxtresources.utils.ItemBuilder;
import net.nxtresources.utils.MsgCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemMgr {

    private static final List<String> ssLore = MsgCache.getList("Items.SetSheep.lore");
    private static final List<String> swLore = MsgCache.getList("Items.SetWaitingLobby.lore");
    private static final List<String> msLore = MsgCache.getList("Items.MapSelector.lore");
    private static final List<String> wlLore = MsgCache.getList("Items.WaitingLobbySelector.lore");

    public static void lobbyItems(Player player){
        ItemStack arenaselector = new ItemBuilder(Material.BOOK)
                .setDisplayName(Main.color(MsgCache.get("Items.ArenaSelector.name")))
                .setAmount(1)
                .setPD(Main.itemData, "ArenaSelector")
                .build();
        player.getInventory().setItem(4, arenaselector);
    }

    //setup items
    public static ItemStack blue = new ItemBuilder(Material.BLUE_WOOL)
            .setDisplayName(Main.color(MsgCache.get("Items.TeamSelector.BLUE.name")))
            .setAmount(1)
            .setPD(Main.itemData, "TeamSelector_Blue")
            .build();
    public static ItemStack red = new ItemBuilder(Material.RED_WOOL)
            .setDisplayName(Main.color(MsgCache.get("Items.TeamSelector.RED.name")))
            .setAmount(1)
            .setPD(Main.itemData, "TeamSelector_Red")
            .build();
    public static ItemStack leaveSetup = new ItemBuilder(Material.BARRIER)
            .setDisplayName(Main.color(MsgCache.get("Items.LeaveSetup.name")))
            .setAmount(1)
            .setPD(Main.itemData, "LeaveSetup")
            .build();
    public static ItemStack saveAndExit = new ItemBuilder(Material.EMERALD_BLOCK)
            .setDisplayName(Main.color(MsgCache.get("Items.SaveAndExit.name")))
            .setAmount(1)
            .setPD(Main.itemData, "SaveAndExit")
            .build();
    public static ItemStack setwaitinglobby = new ItemBuilder(Material.DARK_OAK_DOOR)
            .setDisplayName(Main.color(MsgCache.get("Items.SetWaitingLobby.name")))
            .setAmount(1)
            .setLore(swLore.stream().map(Main::color).toArray(Component[]::new))
            .setPD(Main.itemData, "SetWaitingLobby")
            .build();
    public static ItemStack selectorTool =new ItemBuilder(Material.STONE_AXE)
            .setDisplayName(Main.color(MsgCache.get("Items.MapSelector.name")))
            .setAmount(1)
            .setLore(msLore.stream().map(Main::color).toArray(Component[]::new))
            .setPD(Main.itemData, "MapSelector")
            .build();
    public static ItemStack waitingSelectorTool=new ItemBuilder(Material.GOLDEN_AXE)
            .setDisplayName(Main.color(MsgCache.get("Items.WaitingLobbySelector.name")))
            .setLore(wlLore.stream().map(Main::color).toArray(Component[]::new))
            .setAmount(1)
            .setPD(Main.itemData, "WaitingLobbySelector")
            .build();
    public static ItemStack setRedSheep = new ItemBuilder(Material.RED_CONCRETE)
            .setDisplayName(Main.color(MsgCache.get("Items.SetSheep.name")))
            .setLore(ssLore.stream().map(Main::color).toArray(Component[]::new))
            .setAmount(1)
            .setPD(Main.itemData, "SetRedSheep")
            .build();
    public static ItemStack setBlueSheep = new ItemBuilder(Material.BLUE_CONCRETE)
            .setDisplayName(Main.color(MsgCache.get("Items.SetSheep.name")))
            .setLore(ssLore.stream().map(Main::color).toArray(Component[]::new))
            .setAmount(1)
            .setPD(Main.itemData, "SetBlueSheep")
            .build();

    //default items
    public static ItemStack leaveArena = new ItemBuilder(Material.BARRIER)
            .setDisplayName(Main.color(MsgCache.get("Items.LeaveArena.name")))
            .setAmount(1)
            .setPD(Main.itemData, "LeaveArena")
            .build();

    //sheeps
    public static ItemStack explSheep = new ItemBuilder(Material.PLAYER_HEAD)
            .setDisplayName(Main.color(MsgCache.get("Items.Sheeps.Explosive.name")))
            .setPD(Main.shKey, "EXPLOSIVE")
            .setSkin("ewogICJ0aW1lc3RhbXAiIDogMTcwOTE2NTY1MjEwNywKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjNjZjZmQ5ZjNhNzY1YThjZTUwZDdkNDEzZGZjMjRmMjg2NTU2YzJiNmFkNTA5Njg5OGNmYTkzODBjMzkxNjkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==","q7WLkV0hjPTeKSU5hCQV3y38EBZGL6T2c/mN7ugqo21+wylRGUHPv1If9ubgDZpGYc7hYfoa/TCcPCMkuWZ2Df3o8TIUFmj+vlnduOyrOekITr9crqjioGu6O3DDngB719XYzDeVzrr5420oBPe5TiuqAg3osUjppDr0eCJUXCen69decD9hErmKJvsQn/XCkVOQZSA0h+hgj9zViJxOo57ITmwuV29Ha2zr4g0bNlEoCr9wP/mrA1yWBWvqFmcd1Yh2EWd/22uMSjsbwt7vUnltqyU7++mS6PXcBNsKgE18zUlA2aP/4J+//6g0rxQNjnG8samC75RI5qyIAX4C2el4od7iH/EoPmbnR0BchRzdSVOU19nVhfRyFpzY55vUznmX/FG7Tn+VqO1qhjyfc3ZOto9v5lM5SXGzb1FR1j3j9m+Fef71dYPNZn5bSOve6K0wi1jmuZYL4B1HMiDApckWQXlV7NQPAumrz7PsLaoiE8F01GJUr7/AW8Zeh7Kk5uh4zoJoczYQ7eR1cjgwErUJR3GlwerB2fHB9zO7ffDWwlsqWJcuFvaNzOHuzdItOJ22ULqD22HYJG4vO5gQzvXqg/FkzBTnaQ0k8HUAonpwO97FihG5WLZuA1/uH7RKljMrVosaUV5Kgtd9UIYSa8fUjxVWcOPWTESuFkVgdm0=")
            .build();
    public static ItemStack healingSheep = new ItemBuilder(Material.PLAYER_HEAD)
            .setDisplayName(Main.color(MsgCache.get("Items.Sheeps.Healing.name")))
            .setPD(Main.shKey, "HEALING")
            .setSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzZmNzk3ZDRiN2U3YzcyMWY2ZGYzMTM2ODdiOWY5N2I4MTk1MGVmMWZjOGVmNDk5ODdjZmNmZDA5MTkxYTM0NyJ9fX0=", "")
            .build();

}
