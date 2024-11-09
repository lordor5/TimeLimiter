package org.jeffrey.timeLimiter.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jeffrey.timeLimiter.playerEvents;

public class SetTime implements CommandExecutor {
    playerEvents pEvents;

    public SetTime(playerEvents pEvents) {
        this.pEvents = pEvents;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length != 2) {
            if(sender != null) {
                sender.sendMessage("Please provide a player name and a time!");
            }else{
                Bukkit.getServer().getConsoleSender().sendMessage("Please provide a player name and a time!");
            }
            return false;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if(player == null) {
            if(sender != null) {
                sender.sendMessage("Player "+args[0]+" not found!");
            }else{
                Bukkit.getServer().getConsoleSender().sendMessage("Player "+args[0]+" not found!");
            }
        }
        else {

            if(sender == null || sender.isOp()) {
                int time = pEvents.getPlayerTime(player);
                if(pEvents.setPlayerTime(player, Integer.parseInt(args[1])
                )){
                    if(sender != null) {
                        sender.sendMessage("Player "+args[0]+" had time set to "+args[1]+" minutes from "+time+" minutes!");
                    }else{
                        Bukkit.getServer().getConsoleSender().sendMessage("Player "+args[0]+" had time set to "+args[1]+" minutes from "+time+" minutes!");
                    }
                }

            }else{
                sender.sendMessage("Who are you?");
                return false;
            }
        }

        return true;
    }
}
