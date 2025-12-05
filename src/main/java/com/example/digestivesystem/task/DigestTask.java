package com.example.digestivesystem.task;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle; // 确保导入 Particle
import org.bukkit.Sound;
import org.bukkit.entity.Entity; // 新增导入
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
            if (satiety > 0) {
                double digestSpeed = config.digestSpeed;
                // 素食主义者惩罚
                if (manager.getTrait(player) == PoopManager.Trait.VEGETARIAN) {
                    digestSpeed *= config.vegetarianPenalty;
                }
                
                double amount = Math.min(satiety, digestSpeed);
                manager.setSatiety(player, satiety - amount);
                manager.setPoopLevel(player, manager.getPoopLevel(player) + amount);
            }

            // 2. 提醒逻辑
            double poopLevel = manager.getPoopLevel(player);
            if (poopLevel >= 100) {
                manager.poop(player, true);
            } else if (poopLevel > 90) {
                Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(500));
                Title title = Title.title(config.getMessage("title-warn-main"), config.getMessage("title-warn-sub"), times);
                player.showTitle(title);
                player.sendActionBar(config.getMessage("actionbar-critical", "{amount}", String.format("%.1f", poopLevel)));
            } else if (poopLevel > 70) {
                if (poopLevel % 5 < 1) player.sendActionBar(config.getMessage("actionbar-warn", "{amount}", String.format("%.0f", poopLevel)));
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
                        // 修正点 1: VILLAGER_ANGRY -> ANGRY_VILLAGER
                        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 2, 0), 1);
                    }
                }
            }

            // 4. 踩屎打滑 (检测脚下)
            if (config.enableSlip) {
                // 修正点 2: 1.21 API 变更，改用 getEntities() 遍历
                for (Entity entity : player.getLocation().getChunk().getEntities()) {
                    if (entity instanceof Item item) { // 判断是不是掉落物
                        if (manager.getPoopType(item.getItemStack()) != null) {
                            if (item.getLocation().distanceSquared(player.getLocation()) < 1.0) {
                                 // 滑倒
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
