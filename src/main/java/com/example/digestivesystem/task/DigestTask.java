package com.example.digestivesystem.task;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.time.Duration;

public class DigestTask extends BukkitRunnable {
    private final PoopManager manager;
    private final ConfigManager config;

    public DigestTask(PoopManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // 1. 消化逻辑
            double satiety = manager.getSatiety(player);
            
            // 只有当有饱腹值时，才转换
            if (satiety > 0) {
                double digestSpeed = config.digestSpeed; // 默认 0.5
                
                // 素食主义者惩罚
                if (manager.getTrait(player) == PoopManager.Trait.VEGETARIAN) {
                    digestSpeed *= config.vegetarianPenalty;
                }
                
                // 确保不会扣成负数
                double amount = Math.min(satiety, digestSpeed);
                
                manager.setSatiety(player, satiety - amount);
                manager.setPoopLevel(player, manager.getPoopLevel(player) + amount);
            }

            // 2. 提醒逻辑
            double poopLevel = manager.getPoopLevel(player);
            if (poopLevel >= 100) {
                manager.poop(player, true);
            } else if (poopLevel > 90) {
                // 危险阶段：持续提醒
                Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(500));
                Title title = Title.title(config.getMessage("title-warn-main"), config.getMessage("title-warn-sub"), times);
                player.showTitle(title);
                player.sendActionBar(config.getMessage("actionbar-critical", "{amount}", String.format("%.1f", poopLevel)));
            } else if (poopLevel > 70) {
                // 警告阶段：每秒提醒一次 (Actionbar 不会消失，造成刷屏的错觉，但其实是更新)
                player.sendActionBar(config.getMessage("actionbar-warn", "{amount}", String.format("%.0f", poopLevel)));
            }
            
            // 3. 恶臭系统
            if (config.enableStench) {
                int stenchTime = manager.getStenchTime(player);
                if (stenchTime > 0) {
                    if (player.isInWater()) {
                        manager.setStenchTime(player, 0);
                        player.sendMessage(config.getMessage("stench-end"));
                    } else {
                        manager.setStenchTime(player, stenchTime - 1);
                        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 2, 0), 1);
                    }
                }
            }

            // 4. 踩屎打滑
            if (config.enableSlip) {
                for (Entity entity : player.getLocation().getChunk().getEntities()) {
                    if (entity instanceof Item item) {
                        if (manager.getPoopType(item.getItemStack()) != null) {
                            if (item.getLocation().distanceSquared(player.getLocation()) < 1.0) {
                                 Vector push = player.getLocation().getDirection().multiply(0.8).setY(0.2);
                                 player.setVelocity(push);
                                 if (config.slipSound) {
                                     player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_FALL, 1f, 1f);
                                     player.sendMessage(config.getMessage("slip-msg"));
                                 }
                            }
                        }
                    }
                }
            }
        }
    }
}
