package org.jeffrey.timeLimiter.tracker;

import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerTimeTracker {
    private final Player player;
    private int timeSpent = 0; // Time spent in minutes
    private TimeTracker tTracker;
    private ScheduledExecutorService scheduler;

    public String toString(){
        return player.getDisplayName() +" has "+timeSpent+" minutes";
    }

    public Player getPlayer(){
        return this.player;
    }

    public PlayerTimeTracker(Player player, int userTime, TimeTracker tTracker) {
        this.player = player;
        this.timeSpent = userTime;
        this.tTracker = tTracker;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setPlayerTime(int time){
        timeSpent = time;
    }

    public void playerStartTimer() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            timeSpent++;
            System.out.println("Time spent: " + timeSpent + " minutes for player: "+this.player.getDisplayName());
            if (timeSpent > this.tTracker.pEvents.plugin.getConfig().getInt("timelimit")) {
                tTracker.pEvents.kickPlayer(player);
            }
        }, 1, 1, TimeUnit.MINUTES);
        System.out.println("Timer started.");
    }

    public void playerStopTimer() {
        scheduler.shutdown();
        System.out.println("Timer stopped.");
    }
}
