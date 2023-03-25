package com.arr4nn.buycraftdropparty;

import com.arr4nn.buycraftdropparty.commands.BeginEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


import java.util.Objects;

public final class BuycraftDropParty extends JavaPlugin {
    private static BuycraftDropParty instance;

    public static BuycraftDropParty getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Save default configuration file
        saveDefaultConfig();

        // Register Buycraft listener
        Objects.requireNonNull(getCommand("beginDropEvent")).setExecutor(new BeginEvent(this));
    }

    @Override
    public void onDisable() {
        // Nothing to do here
    }
    public void startDropEvent(Player player, int duration) {
        DropEvent dropEvent = new DropEvent(10, player.getLocation());
        dropEvent.start();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
//                    Bukkit.getScheduler().cancelTasks(plugin);
            }
        }, 0L, 20L);
    }




}
