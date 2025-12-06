package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.enums.BoardType;
import net.nxtresources.enums.SetupStep;
import net.nxtresources.enums.TeamType;
import net.nxtresources.managers.scoreboard.Board;
import net.nxtresources.managers.scoreboard.BoardMgr;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

import static net.nxtresources.managers.ItemMgr.*;

public class SetupMgrNew {

    public static final Map<UUID, SessionData> sessions = new HashMap<>();

    public static class SessionData {

        public final UUID uuid;
        public final String arenaName;
        public final Arena.Temp temp;
        public SetupStep step;

        public SessionData(Player player, String arenaName, int arenaSize){

            this.uuid = player.getUniqueId();
            this.arenaName = arenaName;
            this.temp = new Arena.Temp(arenaName, arenaSize);
            this.step = SetupStep.MAP_REGION;
        }

    }

    public int start(Player player, String name, int size, boolean isTemporary){
        UUID uuid = player.getUniqueId();
        if(sessions.containsKey(uuid)) return 1;

        if(isTemporary) {
            BoardMgr.setBoard(player, new Board(BoardType.SETUPBOARD));
            SessionData session = new SessionData(player, name, size);
            sessions.put(uuid, session);
            player.getInventory().clear();
            player.getInventory().setItem(0, selectorTool);
            player.getInventory().setItem(8, leaveSetup);
            player.getInventory().setItem(7, saveAndExit);
        }
        return 0;
    }

    public static void finish(Player player, boolean success){
        UUID uuid = player.getUniqueId();
        SessionData session = sessions.remove(uuid);
        Arena.Temp tempData = session.temp;
        Arena arena = new Arena(tempData.name, tempData.size);
        if(success){
            arena.setWaitingLobby(tempData.waitingLobby);
            arena.setPos1(tempData.pos1);
            arena.setPos2(tempData.pos2);
            arena.setWaitingPos1(tempData.waitingPos1);
            arena.setWaitingPos2(tempData.waitingPos2);
            arena.wName =tempData.pos1.getWorld().getName();
            arena.setRedSheepSpawns(tempData.redSheepSpawns);
            arena.setBlueSheepSpawns(tempData.blueSheepSpawns);
            for (Map.Entry<String, Location> entry : tempData.teamSpawns.entrySet())
                arena.setTeamSpawn(TeamType.valueOf(entry.getKey()), entry.getValue());
            ArenaMgr.arenas.add(arena);
            ArenaMgr.saveArena(arena);
            player.getInventory().clear();
            ItemMgr.lobbyItems(player);
            WorldMgr.getInst().saveAsync(tempData.pos1.getWorld(),arena.name,arena.pos1,arena.pos2);
            BoardMgr.setBoard(player, new Board(BoardType.LOBBYBOARD));
        } else{
            player.getInventory().clear();
            ItemMgr.lobbyItems(player);
            BoardMgr.setBoard(player, new Board(BoardType.LOBBYBOARD));
        }
    }

    public void giveForNext(Player player, SetupStep step){
        player.getInventory().clear();
        switch (step){
            case MAP_REGION -> player.getInventory().setItem(0, selectorTool);
            case LOBBY_REGION -> player.getInventory().setItem(0, waitingSelectorTool);
            case LOBBY_SPAWN -> player.getInventory().setItem(0, setwaitinglobby);
            case RED_TEAM_SPAWN -> player.getInventory().setItem(0, red);
            case BLUE_TEAM_SPAWN -> player.getInventory().setItem(0, blue);
            case SHEEP_SPAWNS ->{
                player.getInventory().setItem(0, setRedSheep);
                player.getInventory().setItem(1, setBlueSheep);
            }
        }
        player.getInventory().setItem(7, saveAndExit);
        player.getInventory().setItem(8, leaveSetup);
    }

    public void checkStep(Player player){

        UUID uuid = player.getUniqueId();
        SessionData session = sessions.get(uuid);
        SetupStep step = session.step;

        boolean done = switch (step) {
            case MAP_REGION -> session.temp.pos1 != null && session.temp.pos2 != null;
            case LOBBY_REGION -> session.temp.waitingPos1 != null && session.temp.waitingPos2 != null;
            case LOBBY_SPAWN -> session.temp.waitingLobby != null;
            case RED_TEAM_SPAWN -> session.temp.teamSpawns.containsKey("RED");
            case BLUE_TEAM_SPAWN -> session.temp.teamSpawns.containsKey("BLUE");
            case SHEEP_SPAWNS -> !session.temp.redSheepSpawns.isEmpty() && !session.temp.blueSheepSpawns.isEmpty();
        };
        if(!done)return;
        SetupStep next = step.next();
        if(next!=null) {
            session.step = next;
            giveForNext(player, next);
            player.sendMessage(Component.text("Step completed! Next: " + next.name()));
        }
    }

    public static boolean isInSetup(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }
}
