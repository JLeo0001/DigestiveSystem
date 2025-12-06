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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DigestTask extends BukkitRunnable {
    private final PoopManager manager;
    private final ConfigManager config;
    private int tickCounter = 0;
    
    // 用来暂存每个玩家当前的"随机文案"，确保3秒内文案不变
    private final Map<UUID, String> playerCurrentMessage = new HashMap<>();

    public DigestTask(PoopManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    @Override
    public void run() {
        tickCounter++;
        boolean shouldSwitchText = (tickCounter % 3 == 0); // 每3秒切换一次文案

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            // --- 1. 消化逻辑 (数值每秒都在变) ---
            double satiety = manager.getSatiety(player);
            if (satiety > 0) {
                double digestSpeed = config.digestSpeed;
                if (manager.getTrait(player) == PoopManager.Trait.VEGETARIAN) {
                    digestSpeed *= config.vegetarianPenalty;
                }
                double amount = Math.min(satiety, digestSpeed);
                manager.setSatiety(player, satiety - amount);
                manager.setPoopLevel(player, manager.getPoopLevel(player) + amount);
            }

            // --- 2. 提醒逻辑 (核心修改) ---
            double poopLevel = manager.getPoopLevel(player);

            if (poopLevel >= 100) {
                manager.poop(player, true);
                playerCurrentMessage.remove(uuid); // 清理缓存
            } else if (poopLevel > 70) {
                
                // 决定是否要换一条新梗 (每3秒一次)
                // 或者如果玩家刚上线，Map里没数据，也需要获取
                if (shouldSwitchText || !playerCurrentMessage.containsKey(uuid)) {
                    String rawKey = (poopLevel > 90) ? "actionbar-critical" : "actionbar-warn";
                    // 只获取原始字符串 (如 "<red>肚子痛 {amount}%")，不解析变量
                    String newTemplate = config.getRandomRawMessage(rawKey);
                    playerCurrentMessage.put(uuid, newTemplate);
                }

                // 获取暂存的文案模板
                String template = playerCurrentMessage.get(uuid);
                
                // 实时解析：将模板中的 {amount} 替换为当前最新的 poopLevel
                // 这样文案没变，但数值变了
                player.sendActionBar(config.parseMessage(template, "{amount}", String.format("%.1f", poopLevel)));
                
                // 如果极其危险，额外加 Title (Title本身自带淡入淡出，不需要频繁发)
                if (poopLevel > 90 && tickCounter % 3 == 0) {
                    Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(2000), Duration.ofMillis(1000));
                    Title title = Title.title(config.getMessage("title-warn-main"), config.getMessage("title-warn-sub"), times);
                    player.showTitle(title);
                }
            } else {
                // 如果便意消退了(比如刚拉完)，清理缓存防止内存泄漏
                playerCurrentMessage.remove(uuid);
            }
            
            // --- 3. 恶臭与打滑 ---
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

            if (config.enableSlip) {
                for (Entity entity : player.getLocation().getChunk().getEntities()) {
                    if (entity instanceof Item item) {
                        if (manager.getPoopType(item.getItemStack()) != null) {
                            if (item.getLocation().distanceSquared(player.getLocation()) < 1.0) {
                                 Vector push = player.getLocation().getDirection().multiply(0.8).setY(0.2);
                                 player.setVelocity(push);
                                 if (config.slipSound && tickCounter % 2 == 0) {
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
