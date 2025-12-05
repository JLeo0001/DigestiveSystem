package com.example.digestivesystem.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PoopManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final NamespacedKey KEY_SATIETY;
    private final NamespacedKey KEY_POOP_LEVEL;
    private final NamespacedKey KEY_HAS_EATEN_GOLD;
    private final NamespacedKey KEY_IS_POOP_ITEM;

    public PoopManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.KEY_SATIETY = new NamespacedKey(plugin, "satiety");
        this.KEY_POOP_LEVEL = new NamespacedKey(plugin, "poop_level");
        this.KEY_HAS_EATEN_GOLD = new NamespacedKey(plugin, "has_eaten_gold");
        this.KEY_IS_POOP_ITEM = new NamespacedKey(plugin, "is_poop_item");
    }

    public double getSatiety(Player p) {
        return p.getPersistentDataContainer().getOrDefault(KEY_SATIETY, PersistentDataType.DOUBLE, 0.0);
    }
    public void setSatiety(Player p, double val) {
        p.getPersistentDataContainer().set(KEY_SATIETY, PersistentDataType.DOUBLE, Math.min(100.0, Math.max(0, val)));
    }
    public double getPoopLevel(Player p) {
        return p.getPersistentDataContainer().getOrDefault(KEY_POOP_LEVEL, PersistentDataType.DOUBLE, 0.0);
    }
    public void setPoopLevel(Player p, double val) {
        p.getPersistentDataContainer().set(KEY_POOP_LEVEL, PersistentDataType.DOUBLE, Math.min(100.0, Math.max(0, val)));
    }
    public void markGoldenEaten(Player p, boolean eaten) {
        if (eaten) p.getPersistentDataContainer().set(KEY_HAS_EATEN_GOLD, PersistentDataType.BYTE, (byte) 1);
        else p.getPersistentDataContainer().remove(KEY_HAS_EATEN_GOLD);
    }
    public boolean hasEatenGold(Player p) {
        return p.getPersistentDataContainer().has(KEY_HAS_EATEN_GOLD, PersistentDataType.BYTE);
    }

    public void poop(Player player, boolean forced) {
        setPoopLevel(player, 0.0);
        boolean isGold = hasEatenGold(player);
        markGoldenEaten(player, false);

        if (forced) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 2f, 0.5f);
            Particle.DustOptions color = isGold ? new Particle.DustOptions(Color.YELLOW, 2.0f) : new Particle.DustOptions(Color.fromRGB(102, 51, 0), 2.0f);
            player.getWorld().spawnParticle(Particle.DUST, player.getLocation(), 50, 0.5, 0.5, 0.5, color);
            if (config.explodeDamage) player.getWorld().createExplosion(player.getLocation(), 2.0f);
            player.sendMessage(config.getMessage("poop-explode"));
        } else {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 0.5f);
            player.sendMessage(config.getMessage("poop-success"));
        }

        ItemStack poopItem;
        if (isGold) {
            poopItem = new ItemStack(Material.GOLD_NUGGET);
            ItemMeta meta = poopItem.getItemMeta();
            meta.displayName(config.getMessage("item-gold-poop-name"));
            meta.lore(config.getMessageList("item-gold-poop-lore"));
            meta.getPersistentDataContainer().set(KEY_IS_POOP_ITEM, PersistentDataType.STRING, "GOLD");
            poopItem.setItemMeta(meta);
        } else {
            poopItem = new ItemStack(Material.COOKED_BEEF);
            ItemMeta meta = poopItem.getItemMeta();
            meta.displayName(config.getMessage("item-poop-name"));
            meta.lore(config.getMessageList("item-poop-lore"));
            meta.getPersistentDataContainer().set(KEY_IS_POOP_ITEM, PersistentDataType.STRING, "NORMAL");
            poopItem.setItemMeta(meta);
        }
        player.getWorld().dropItemNaturally(player.getLocation(), poopItem);
    }
    
    public String getPoopType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(KEY_IS_POOP_ITEM, PersistentDataType.STRING);
    }

    public boolean tryToiletPoop(Player player) {
        Block targetBlock = player.getTargetBlockExact(3);
        if (targetBlock != null && targetBlock.getType() == Material.CAULDRON) {
            setPoopLevel(player, 0.0);
            player.getWorld().playSound(targetBlock.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1f, 1f);
            player.getWorld().playSound(targetBlock.getLocation(), Sound.BLOCK_WATER_AMBIENT, 1f, 1f);
            player.sendMessage(config.getMessage("toilet-use"));
            return true;
        }
        return false;
    }
}
