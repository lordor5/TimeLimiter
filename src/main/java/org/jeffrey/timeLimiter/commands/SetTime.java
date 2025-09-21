package org.jeffrey.timeLimiter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jeffrey.timeLimiter.playerEvents;

public class SetTime implements CommandExecutor {
    
    private final playerEvents pEvents;
    
    public SetTime(playerEvents pEvents) {
        this.pEvents = pEvents;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("timelimiter.settime")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }
        
        // Validate arguments
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /settime <player> <minutes>");
            sender.sendMessage("§7Example: /settime PlayerName 60");
            return true;
        }
        
        String targetPlayerName = args[0];
        String timeString = args[1];
        
        // Parse time
        int newTime;
        try {
            newTime = Integer.parseInt(timeString);
            if (newTime < 0) {
                sender.sendMessage("§cTime must be a positive number or zero.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid time value. Please enter a valid number.");
            return true;
        }
        
        // Find target player (online)
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        
        if (targetPlayer != null) {
            // Player is online
            boolean success = pEvents.setPlayerTime(targetPlayer, newTime);
            
            if (success) {
                sender.sendMessage("§aSuccessfully set " + targetPlayer.getName() + "'s time to " + newTime + " minutes.");
                
                // Notify the target player
                if (!sender.equals(targetPlayer)) {
                    targetPlayer.sendMessage("§6Your play time has been set to " + newTime + " minutes by " + sender.getName() + ".");
                }
                
                // Check if player should be kicked after setting time
                int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                if (newTime >= timeLimit) {
                    sender.sendMessage("§eWarning: Player's time (" + newTime + ") meets or exceeds the limit (" + timeLimit + "). They may be kicked.");
                }
            } else {
                sender.sendMessage("§cFailed to set time for " + targetPlayer.getName() + ". Please try again.");
            }
        } else {
            // Player is not online - try to find them by name in saved data or use UUID lookup
            // First, try to get the player's UUID from Bukkit (this works for players who have joined before)
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
            
            if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                // We found the offline player
                String playerUUID = offlinePlayer.getUniqueId().toString();
                boolean success = pEvents.tTracker.setPlayerTimeByUUID(playerUUID, newTime);
                
                if (success) {
                    sender.sendMessage("§aSuccessfully set " + offlinePlayer.getName() + "'s (offline) time to " + newTime + " minutes.");
                    
                    // Check if player should be kicked when they join
                    int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                    if (newTime >= timeLimit) {
                        sender.sendMessage("§eWarning: Player's time (" + newTime + ") meets or exceeds the limit (" + timeLimit + "). They may be kicked when they join.");
                    }
                } else {
                    sender.sendMessage("§cFailed to set time for " + offlinePlayer.getName() + ". Please try again.");
                }
            } else {
                sender.sendMessage("§cPlayer '" + targetPlayerName + "' not found. They may have never joined this server.");
                sender.sendMessage("§7Note: You can only set time for players who have joined the server at least once.");
            }
        }
        
        return true;
    }
}