package com.arr4nn.buycraftdropparty.commands;

import com.arr4nn.buycraftdropparty.BuycraftDropParty;
import com.arr4nn.buycraftdropparty.DropEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BeginEvent implements CommandExecutor {
    BuycraftDropParty plugin;

    public BeginEvent(BuycraftDropParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof ConsoleCommandSender)) return false;
        String playerName = args[0];
        Player player = Bukkit.getServer().getPlayer(playerName);
        if(player == null){
            sender.sendMessage("This player does not exist.");
            return false;
        }
        if(player.isOnline()){
            sender.sendMessage("Begun the drop party for "+player.getName());
            DropEvent dropevent = new DropEvent(10, player.getLocation());
            dropevent.start();

        }


        return true;
    }
}
