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
import org.bukkit.inventory.ItemStack;
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
        
        // 1. 吃屎逻辑
        if (poopType != null) {
            // 延迟一tick执行属性修改，确保覆盖原版食物效果（虽然牛排本身有效果，但我们要强制覆盖）
            if (poopType.equals("GOLD")) {
                player.sendMessage(configManager.getMessage("eat-poop-gold"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                
                // 设置配置的数值
                poopManager.setSatiety(player, poopManager.getSatiety(player) + configManager.goldEatSatiety);
                // 修改饥饿值和饱和度 (需延迟或直接设定)
                // 简单做法：直接增加，注意边界
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + configManager.goldEatFood));
                player.setSaturation(Math.min(20f, player.getSaturation() + configManager.goldEatSaturation));

            } else {
                player.sendMessage(configManager.getMessage("eat-poop-normal"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2));
                
                // 设置配置的数值
                poopManager.setSatiety(player, poopManager.getSatiety(player) + configManager.normalEatSatiety);
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + configManager.normalEatFood));
                player.setSaturation(Math.min(20f, player.getSaturation() + configManager.normalEatSaturation));
            }
            return;
        }

        // 2. 正常食物 & 体质逻辑
        Material mat = item.getType();
        double value = configManager.getFoodValue(mat);
        PoopManager.Trait trait = poopManager.getTrait(player);

        if (mat == Material.MILK_BUCKET) {
            if (trait == PoopManager.Trait.LACTOSE_INTOLERANT) {
                poopManager.setPoopLevel(player, poopManager.getPoopLevel(player) + configManager.lactosePenalty);
                player.sendMessage(configManager.getMessage("trait-trigger-lactose"));
            }
        }
        
        if (mat == Material.ROTTEN_FLESH && trait == PoopManager.Trait.IRON_STOMACH) {
            player.removePotionEffect(PotionEffectType.HUNGER);
            value *= 1.5; 
        }

        if (value > 0) {
            poopManager.setSatiety(player, poopManager.getSatiety(player) + value);
            if (mat == configManager.specialTriggerFood) {
                poopManager.markGoldenEaten(player, true);
            }
        }
    }

    @EventHandler
    public void onVillagerTrade(PlayerInteractEntityEvent event) {
        if (!configManager.enableStench || !configManager.stenchRefuseTrade) return;
        if (event.getRightClicked() instanceof Villager) {
            if (poopManager.getStenchTime(event.getPlayer()) > 0) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(configManager.getMessage("stench-refuse"));
                ((Villager) event.getRightClicked()).shakeHead();
            }
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (event.getView().title().equals(configManager.getMessage("gui-title"))) {
            event.setCancelled(true);
        }
    }
}
