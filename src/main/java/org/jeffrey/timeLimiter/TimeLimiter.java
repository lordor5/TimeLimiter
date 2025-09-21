package org.jeffrey.timeLimiter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jeffrey.timeLimiter.commands.Check;
import org.jeffrey.timeLimiter.commands.SetTime;

import java.util.Objects;

public final class TimeLimiter extends JavaPlugin {
    private playerEvents pEvents = new playerEvents(this);
    private FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        config.addDefault("timelimit", 120);
        config.addDefault("timePerHour", 3);
        config.options().copyDefaults(true);
        saveConfig();
        pEvents = new playerEvents(this);
        pEvents.init();
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new Check(pEvents));
        Objects.requireNonNull(this.getCommand("settime")).setExecutor(new SetTime(pEvents));
        getServer().getPluginManager().registerEvents(pEvents, this);
        
        // Schedule hourly time update
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::updateTimeLimits, 72000L, 72000L); // 72000 ticks = 1 hour
    }

    private void updateTimeLimits() {
        reloadConfig(); // Reload the config file to get updated values
        double timePerHour = getConfig().getDouble("timePerHour");
        double currentLimit = getConfig().getDouble("timelimit");
        getConfig().set("timelimit", currentLimit + timePerHour);
        System.out.println("Updating time limits to: " + (currentLimit + timePerHour));
        saveConfig();
    }

    @Override
    public void onDisable() {
        pEvents.saveAll();
    }

}