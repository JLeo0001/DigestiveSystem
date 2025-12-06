package com.example.digestivesystem.listener;

import com.example.digestivesystem.gui.StomachGui; // 新增
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
    private final StomachGui stomachGui; // 新增

    public PlayerListener(PoopManager poopManager, ConfigManager configManager, StomachGui stomachGui) {
        this.poopManager = poopManager;
        this.configManager = configManager;
        this.stomachGui = stomachGui;
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        // ... (保持原有的吃东西代码不变，这里省略以节省篇幅，请直接保留你上一次的 PlayerListener 内容) ...
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        String poopType = poopManager.getPoopType(item);
        
        if (poopType != null) {
            if (poopType.equals("GOLD")) {
                player.sendMessage(configManager.getMessage("eat-poop-gold"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                poopManager.setSatiety(player, poopManager.getSatiety(player) + configManager.goldEatSatiety);
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + configManager.goldEatFood));
                player.setSaturation(Math.min(20f, player.getSaturation() + configManager.goldEatSaturation));
            } else {
                player.sendMessage(configManager.getMessage("eat-poop-normal"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2));
                poopManager.setSatiety(player, poopManager.getSatiety(player) + configManager.normalEatSatiety);
                player.setFoodLevel(Math.min(20, player.getFoodLevel() + configManager.normalEatFood));
                player.setSaturation(Math.min(20f, player.getSaturation() + configManager.normalEatSaturation));
            }
            return;
        }

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
        // ... (保持不变) ...
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
        Component title = event.getView().title();
        
        // 1. 肠胃系统主界面
        if (title.equals(configManager.getMessage("gui-title"))) {
            event.setCancelled(true);
            // 检查是否点击了金锭(排行榜按钮)
            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.GOLD_INGOT) {
                stomachGui.openRank((Player) event.getWhoClicked());
            }
        }
        
        // 2. 排行榜界面
        if (title.equals(configManager.getMessage("rank-gui-title"))) {
            event.setCancelled(true);
        }
    }
}
