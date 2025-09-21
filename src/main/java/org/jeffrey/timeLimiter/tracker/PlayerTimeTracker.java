package org.jeffrey.timeLimiter.tracker;

import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerTimeTracker {
    private final Player player;
    private int timeSpent;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerTask;
    private final TimeTracker tTracker;

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
        // Stop any existing timer first
        playerStopTimer();
        
        scheduler = Executors.newScheduledThreadPool(1);
        timerTask = scheduler.scheduleAtFixedRate(() -> {
            // Check if player is still online before incrementing
            if(!player.isOnline()) {
                System.out.println("Player " + player.getDisplayName() + " is no longer online, stopping timer");
                playerStopTimer();
                return;
            }
            
            timeSpent++;
            int timeLimit = this.tTracker.pEvents.plugin.getConfig().getInt("timelimit");
            System.out.println("Time spent: " + timeSpent + " minutes for player: "+this.player.getDisplayName());
            
            if (timeSpent >= timeLimit) { // Changed > to >= for consistency
                tTracker.kickPlayer(player);
                playerStopTimer(); // Stop timer after kicking
            }
        }, 1, 1, TimeUnit.MINUTES);
        System.out.println("Timer started for: " + player.getDisplayName());
    }

    public void playerStopTimer() {
        if (timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel(false);
            timerTask = null;
        }
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        System.out.println("Timer stopped for: " + player.getDisplayName());
    }
}