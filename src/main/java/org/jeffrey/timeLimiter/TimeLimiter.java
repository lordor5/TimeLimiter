package org.jeffrey.timeLimiter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jeffrey.timeLimiter.commands.Check;
import org.jeffrey.timeLimiter.commands.SetTime;

import java.util.Objects;

public final class TimeLimiter extends JavaPlugin {
    playerEvents pEvents = new playerEvents(this);
    FileConfiguration config = this.getConfig();

    @Override
    public void onEnable() {
        config.addDefault("timelimit", 120);
        config.options().copyDefaults(true);
        saveConfig();
        pEvents = new playerEvents(this);
        pEvents.init();
        Objects.requireNonNull(this.getCommand("check")).setExecutor(new Check(pEvents));
        Objects.requireNonNull(this.getCommand("settime")).setExecutor(new SetTime(pEvents));
        getServer().getPluginManager().registerEvents(pEvents, this);
    }

    @Override
    public void onDisable() {
        pEvents.saveAll();
    }

}