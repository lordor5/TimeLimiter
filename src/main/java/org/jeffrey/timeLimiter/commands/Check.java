package org.jeffrey.timeLimiter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jeffrey.timeLimiter.playerEvents;


public class Check implements CommandExecutor {

    playerEvents pEvents;

    public Check(playerEvents pEvents) {
        this.pEvents = pEvents;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if(args.length != 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if(p != null) {
                player = p;
            }
        }

        if(player == null) {
            Bukkit.getServer().getConsoleSender().sendMessage("Please provide a player name!");
            return false;
        }
        else {
            int time = pEvents.getPlayerTime(player);
            if(sender != null) {
                player.sendMessage(player.getDisplayName()+" has played on the server for "+time+" minutes today!");
            }else{
                Bukkit.getServer().getConsoleSender().sendMessage(player.getDisplayName()+" has played on the server for "+time+" minutes today!");
            }
        }

        return true;
    }
}
