package net.nxtresources.managers.scoreboard;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

public abstract class BaseBoard {
    ScoreboardManager sbm;
    Scoreboard sb;
    Objective obj;
    Player player;
    BukkitTask task;

    public BaseBoard() {
        this.sbm = Bukkit.getScoreboardManager();
        this.sb = sbm.getNewScoreboard();
        this.obj = sb.registerNewObjective("fancysheepwars", Criteria.DUMMY, Component.text("Fancy Sheepwars"));
        this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public void line(int score, String prefix, String suffix) {
        String line="line"+score;
        String entry = "§" + score;

        Team team = sb.getTeam(line + score);
        if(team == null) {
            team = sb.registerNewTeam(line + score);
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> update(player), 0L, 20L);
    }

    public void set(Player player){
        this.player = player;
        player.setScoreboard(this.sb);
    }

    public abstract void build(Player player);
    public abstract void update(Player player);

}
