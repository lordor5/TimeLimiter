package org.jeffrey.timeLimiter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jeffrey.timeLimiter.tracker.TimeTracker;

import java.util.Objects;

import static org.bukkit.Bukkit.getServer;

public class playerEvents implements Listener {

    public TimeLimiter plugin;
    TimeTracker tTracker;
    public playerEvents(TimeLimiter plugin) {
        this.plugin=plugin;
        this.tTracker = new TimeTracker(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        onPlayerLogin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerQuitEvent(PlayerQuitEvent event) {
        onPlayerLeave(event.getPlayer());
    }

    void onPlayerLogin(Player player) {
        tTracker.startTimer(player);
    }
    void onPlayerLeave(Player player) {
        tTracker.stopTimer(player);
    }

    public void kickPlayer(Player player) {
        if(!player.hasPermission("timelimiter.bypasskick")){
            System.out.println("Kicking player");
            getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.kickPlayer("Out of time!");
            });
        }

    }

    public int getPlayerTime(Player player) {
        return tTracker.getPlayerTime(player);
    }

    public boolean setPlayerTime(Player player, int time) {
        return tTracker.setPlayerTime(player, time);
    }

    public void saveAll(){
        tTracker.saveAll();
    }
    public void init(){
        Bukkit.getServer().getOnlinePlayers().forEach(this::onPlayerLogin);
    }
}
