package org.jeffrey.timeLimiter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jeffrey.timeLimiter.playerEvents;

public class Check implements CommandExecutor {
    
    private final playerEvents pEvents;
    
    public Check(playerEvents pEvents) {
        this.pEvents = pEvents;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments provided
        if (args.length == 0) {
            if (sender instanceof Player) {
                // Player checking their own time
                Player player = (Player) sender;
                int timeSpent = pEvents.getPlayerTime(player);
                int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                int timeRemaining = Math.max(0, timeLimit - timeSpent);
                
                player.sendMessage("§6Time Information:");
                player.sendMessage("§7Time spent: §e" + timeSpent + " minutes");
                player.sendMessage("§7Time limit: §e" + timeLimit + " minutes");
                player.sendMessage("§7Time remaining: §e" + timeRemaining + " minutes");
                return true;
            } else {
                // Console without arguments - show all online players
                sender.sendMessage("§6Online Players Time Information:");
                boolean hasPlayers = false;
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    int timeSpent = pEvents.getPlayerTime(onlinePlayer);
                    int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                    int timeRemaining = Math.max(0, timeLimit - timeSpent);
                    
                    sender.sendMessage("§7" + onlinePlayer.getName() + ": §e" + timeSpent + "§7/§e" + timeLimit + 
                                     " minutes (§e" + timeRemaining + " remaining§7)");
                    hasPlayers = true;
                }
                if (!hasPlayers) {
                    sender.sendMessage("§7No players currently online.");
                }
                return true;
            }
        }
        
        // If one argument provided (player name)
        if (args.length == 1) {
            String targetPlayerName = args[0];
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            
            if (targetPlayer != null) {
                // Player is online
                // Check if sender has permission to check other players (if you want to add this)
                if (sender instanceof Player && !sender.hasPermission("timelimiter.check.others")) {
                    sender.sendMessage("§cYou don't have permission to check other players' time.");
                    return true;
                }
                
                int timeSpent = pEvents.getPlayerTime(targetPlayer);
                int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                int timeRemaining = Math.max(0, timeLimit - timeSpent);
                
                sender.sendMessage("§6Time Information for " + targetPlayer.getName() + " (Online):");
                sender.sendMessage("§7Time spent: §e" + timeSpent + " minutes");
                sender.sendMessage("§7Time limit: §e" + timeLimit + " minutes");
                sender.sendMessage("§7Time remaining: §e" + timeRemaining + " minutes");
            } else {
                // Player is not online - try to find offline player
                @SuppressWarnings("deprecation")
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName);
                
                if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                    // Check permissions for offline players too
                    if (sender instanceof Player && !sender.hasPermission("timelimiter.check.others")) {
                        sender.sendMessage("§cYou don't have permission to check other players' time.");
                        return true;
                    }
                    
                    String playerUUID = offlinePlayer.getUniqueId().toString();
                    int timeSpent = pEvents.tTracker.getPlayerTimeByUUID(playerUUID);
                    int timeLimit = pEvents.plugin.getConfig().getInt("timelimit");
                    int timeRemaining = Math.max(0, timeLimit - timeSpent);
                    
                    sender.sendMessage("§6Time Information for " + offlinePlayer.getName() + " (Offline):");
                    sender.sendMessage("§7Time spent: §e" + timeSpent + " minutes");
                    sender.sendMessage("§7Time limit: §e" + timeLimit + " minutes");
                    sender.sendMessage("§7Time remaining: §e" + timeRemaining + " minutes");
                    
                    // Show last seen info
                    long lastSeen = offlinePlayer.getLastPlayed();
                    if (lastSeen > 0) {
                        long timeSinceLastSeen = (System.currentTimeMillis() - lastSeen) / 1000 / 60; // minutes
                        sender.sendMessage("§7Last seen: §e" + timeSinceLastSeen + " minutes ago");
                    }
                } else {
                    sender.sendMessage("§cPlayer '" + targetPlayerName + "' not found or has never joined this server.");
                }
            }
            return true;
        }
        
        // Too many arguments
        sender.sendMessage("§cUsage: /check [player]");
        sender.sendMessage("§7- Use '/check' to check your own time (players only)");
        sender.sendMessage("§7- Use '/check <player>' to check a specific player's time");
        return true;
    }
}