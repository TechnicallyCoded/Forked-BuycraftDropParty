package com.arr4nn.buycraftdropparty;


import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DropEvent {

    private static final int PRE_EVENT_SECONDS = 5;
    private static final int ENDING_COUNTDOWN_SECONDS = 10;

    // Duration in seconds
    private final int dropDuration;
    private final Location location;
    private int taskId;

    public DropEvent(int duration, Location location) {
        this.dropDuration = duration;
        this.location = location;
    }

    public void start() {

        // Get list of items from configuration file
        List<String> configItems = BuycraftDropParty.getInstance().getConfig().getStringList("items");
        int maxItems = BuycraftDropParty.getInstance().getConfig().getInt("max_items");
        int radius = BuycraftDropParty.getInstance().getConfig().getInt("radius");

        Random random = new Random();

        BukkitScheduler scheduler = Bukkit.getScheduler();
        taskId = new BukkitRunnable() {
            int ticksDuration = PRE_EVENT_SECONDS * 20;
            final int interval = 20; // drop items every 20 ticks

            boolean dropMode = false;
            int ticks = 0;

            @Override
            public void run() {
                final int ticksLeft = ticksDuration - ticks;
                final int secondsLeft = ticksLeft / 20;

                if (ticksLeft <= 0) {
                    // Done with drop event, stop
                    if (dropMode) {
                        this.cancel();
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party ended!");
                    }
                    // Done with countdown, switch to drop event and reset countdown
                    else {
                        ticksDuration = dropDuration * 20;
                        dropMode = true;
                    }
                    return;
                }


                // Chat announcement only once per second
                if (ticks % 20 == 0) {
                    // Not in drop mode, count down the start timer
                    if (!dropMode) {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party starting in " + secondsLeft + " seconds!");
                    }
                    // Only show ending countdown when the event is
                    else if (secondsLeft <= ENDING_COUNTDOWN_SECONDS) {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party ending in " + secondsLeft + " seconds!");
                    }
                }

                // Drop items only in drop mode and at the requested interval
                if (dropMode && ticks % interval == 0) {
                    System.out.println("spawn!");
                    List<String> items = new ArrayList<>(configItems);

                    // Drop random items within radius
                    for (int i = 0; i < maxItems; i++) {
                        if (items.isEmpty()) {
                            break;
                        }

                        String itemName = items.remove(random.nextInt(items.size()));
                        Material itemMaterial = Material.getMaterial(itemName.toUpperCase());
                        if (itemMaterial == null) {
                            continue;
                        }

                        Location itemLocation = getRandomOffset(location.getX(), location.getY(), location.getZ(), radius);
                        Objects.requireNonNull(itemLocation.getWorld()).dropItemNaturally(itemLocation, new ItemStack(itemMaterial));
                        // Play firework effect
                        itemLocation.getWorld().spawn(itemLocation.add(0, 1, 0), Firework.class).detonate();
                        itemLocation.subtract(0, 1, 0);

                        // Spawn firework effect 1s later
                        scheduler.scheduleSyncDelayedTask(BuycraftDropParty.getInstance(), () -> {
                            itemLocation.getWorld().spawn(itemLocation.add(0, 1, 0), Firework.class).detonate();
                        }, 20L);
                        // Play smoke effect 1 block under, 1/2 second later
                        scheduler.scheduleSyncDelayedTask(BuycraftDropParty.getInstance(), () -> {
                            itemLocation.subtract(0, 1, 0);
                            itemLocation.getWorld().playEffect(itemLocation, Effect.SMOKE, 4);
                        }, 30L);
                    }
                }

                ticks++;
            }
        }.runTaskTimer(BuycraftDropParty.getInstance(), 0L, 1L).getTaskId();
    }

    private Location getRandomOffset(double x, double y, double z, int radius) {
        Random random = new Random();

        int offsetX = random.nextInt(radius * 2 + 1) - radius;
        int offsetY = random.nextInt(radius * 2 + 1) - radius;
        int offsetZ = random.nextInt(radius * 2 + 1) - radius;

        return new Location(Bukkit.getWorlds().get(0), x + offsetX, y + offsetY, z + offsetZ);
    }

    public boolean isRunning() {
        if (taskId != 0) {
            Bukkit.getScheduler().isQueued(taskId);
        }
        return Bukkit.getScheduler().isCurrentlyRunning(taskId);
    }
}

