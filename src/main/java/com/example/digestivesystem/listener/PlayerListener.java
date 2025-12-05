package com.example.digestivesystem.listener;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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
        String poopType = poopManager.getPoopType(event.getItem());
        
        if (poopType != null) {
            if (poopType.equals("GOLD")) {
                player.sendMessage(configManager.getMessage("eat-poop-gold"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, 1200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            } else {
                player.sendMessage(configManager.getMessage("eat-poop-normal"));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2));
                poopManager.setSatiety(player, poopManager.getSatiety(player) + 30.0);
            }
            return;
        }

        Material mat = event.getItem().getType();
        double value = configManager.getFoodValue(mat);
        if (value > 0) {
            poopManager.setSatiety(player, poopManager.getSatiety(player) + value);
            if (mat == configManager.specialTriggerFood) poopManager.markGoldenEaten(player, true);
        }
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (event.getView().title().equals(configManager.getMessage("gui-title"))) event.setCancelled(true);
    }
}
