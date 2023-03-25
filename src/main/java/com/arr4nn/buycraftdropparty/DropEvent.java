package com.arr4nn.buycraftdropparty;


import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DropEvent {
    private final int duration;
    private final Location location;
    private int taskId;

    public DropEvent(int duration, Location location) {
        this.duration = duration;
        this.location = location;
    }

    public void start() {
        final int[] ticksLeft = {duration * 20};
        int interval = Math.max(ticksLeft[0] / 10, 20); // drop items every 2 seconds, but at least 20 ticks

        BukkitScheduler scheduler = Bukkit.getScheduler();
        taskId = scheduler.scheduleSyncRepeatingTask(BuycraftDropParty.getInstance(), new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticksLeft[0] == duration * 20) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party starting in " + duration + " seconds!");
                } else if (ticksLeft[0] <= interval * 10) {
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party ending in " + (ticksLeft[0] / 20) + " seconds!");
                }

                if (ticks % interval == 0) {
                    // Get list of items from configuration file
                    List<String> items = BuycraftDropParty.getInstance().getConfig().getStringList("items");
                    int maxItems = BuycraftDropParty.getInstance().getConfig().getInt("max_items");
                    int radius = BuycraftDropParty.getInstance().getConfig().getInt("radius");

                    // Drop random items within radius
                    for (int i = 0; i < maxItems; i++) {
                        if (items.isEmpty()) {
                            break;
                        }

                        String itemName = items.remove(new Random().nextInt(items.size()));
                        Material itemMaterial = Material.getMaterial(itemName.toUpperCase());
                        if (itemMaterial == null) {
                            continue;
                        }

                        Location itemLocation = getRandomOffset(location.getX(), location.getY(), location.getZ(), radius);
                        Objects.requireNonNull(itemLocation.getWorld()).dropItemNaturally(itemLocation, new ItemStack(itemMaterial));
                        // Play firework effect
                        itemLocation.getWorld().spawn(itemLocation.add(0, 1, 0), Firework.class).detonate();
                        itemLocation.subtract(0, 1, 0);
                        scheduler.scheduleSyncDelayedTask(BuycraftDropParty.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                itemLocation.getWorld().spawn(itemLocation.add(0, 1, 0), Firework.class).detonate();
                                itemLocation.subtract(0, 1, 0);
                            }
                        }, 20L);
                        scheduler.scheduleSyncDelayedTask(BuycraftDropParty.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                itemLocation.getWorld().playEffect(itemLocation, Effect.SMOKE, 4);
                            }
                        }, 30L);
                    }
                }

                ticks++;
                ticksLeft[0]--;

                if (ticksLeft[0] <= 0) {
                    ((BukkitScheduler) scheduler).cancelTask(taskId);
                    Bukkit.broadcastMessage(ChatColor.YELLOW + "Drop party ended!");
                }
            }
        }, 0L, 1L);
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

