package net.nxtresources.managers.scoreboard;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.UUID;

public abstract class BaseBoard {
    ScoreboardManager sbm;
    Scoreboard sb;
    Objective obj;
    Player player;
    BukkitTask task;

    public BaseBoard() {
        this.sbm = Bukkit.getScoreboardManager();
        this.sb = sbm.getNewScoreboard();
        this.obj = sb.registerNewObjective("fsw_" + UUID.randomUUID().toString().substring(0,6), Criteria.DUMMY, Component.text("Fancy SheepWars"));
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public synchronized void line(int score, String prefix, String suffix) {
        String line="line"+score;
        String entry = "§" + score;

        Team team = sb.getTeam(line);
        if(team == null) {
            team = sb.registerNewTeam(line);
            team.addEntry(entry);
        }
        team.prefix(Main.color(prefix));
        team.suffix(Main.color(suffix));
        obj.getScore(entry).setScore(score);
    }

    public void title(Component content){
        obj.displayName(content);
    }

    public void updateB() {
        task=Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> update(player), 0L, 20L);
    }

    /*public void set(Player player){
        this.player = player;
        task=Bukkit.getScheduler().runTask(Main.getInstance(), () -> player.setScoreboard(this.sb));
    }*/
    public void set(Player player){
        if(player==null || !player.isOnline()) return;
        this.player = player;
        player.setScoreboard(this.sb);
    }

    public void cancel(){
        task.cancel();
    }

    public abstract void build(Player player);
    public abstract void update(Player player);

}
