package com.example.digestivesystem.listener;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack; // 关键修复：已添加缺失的导入
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
    private final PoopManager poopManager;
    private final ConfigManager configManager;

    public PlayerListener(PoopManager poopManager, ConfigManager configManager) {
        this.poopManager = poopManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        String poopType = poopManager.getPoopType(item);
        
        // 1. 吃屎逻辑 (特殊物品)
        if (poopType != null) {
            if (poopType.equals("GOLD")) {
                // 随机消息：金屎
                player.sendMessage(configManager.getMessage("eat-poop-gold"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            } else {
                // 随机消息：普通屎
                player.sendMessage(configManager.getMessage("eat-poop-normal"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2));
                // 吃屎反而会增加大量积便值 (恶性循环)
                poopManager.setSatiety(player, poopManager.getSatiety(player) + 30.0);
            }
            return;
        }

        // 2. 正常食物 & 体质逻辑
        Material mat = item.getType();
        double value = configManager.getFoodValue(mat);
        PoopManager.Trait trait = poopManager.getTrait(player);

        // 乳糖不耐受检测
        if (mat == Material.MILK_BUCKET) {
            if (trait == PoopManager.Trait.LACTOSE_INTOLERANT) {
                // 增加积便值惩罚
                poopManager.setPoopLevel(player, poopManager.getPoopLevel(player) + configManager.lactosePenalty);
                // 播放随机警告消息
                player.sendMessage(configManager.getMessage("trait-trigger-lactose"));
            }
        }
        
        // 钢铁之胃吃腐肉无视负面并加倍吸收
        if (mat == Material.ROTTEN_FLESH && trait == PoopManager.Trait.IRON_STOMACH) {
            player.removePotionEffect(PotionEffectType.HUNGER);
            value *= 1.5; 
        }

        // 增加饱腹值
        if (value > 0) {
            poopManager.setSatiety(player, poopManager.getSatiety(player) + value);
            // 检查金苹果触发条件
            if (mat == configManager.specialTriggerFood) {
                poopManager.markGoldenEaten(player, true);
            }
        }
    }

    @EventHandler
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        // 恶臭导致村民拒绝交易逻辑
        if (!configManager.enableStench || !configManager.stenchRefuseTrade) return;
        
        if (event.getRightClicked() instanceof Villager) {
            if (poopManager.getStenchTime(event.getPlayer()) > 0) {
                event.setCancelled(true);
                // 随机拒绝消息
                event.getPlayer().sendMessage(configManager.getMessage("stench-refuse"));
                ((Villager) event.getRightClicked()).shakeHead();
            }
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        // 防止拿走 GUI 里的图标
        if (event.getView().title().equals(configManager.getMessage("gui-title"))) {
            event.setCancelled(true);
        }
    }
}
