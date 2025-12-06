package com.example.digestivesystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage; // 新增导入
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;

public class PoopManager {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    
    private final NamespacedKey KEY_SATIETY;
    private final NamespacedKey KEY_POOP_LEVEL;
    private final NamespacedKey KEY_HAS_EATEN_GOLD;
    private final NamespacedKey KEY_IS_POOP_ITEM;
    private final NamespacedKey KEY_TRAIT; 
    private final NamespacedKey KEY_STENCH_TIME; 

    public enum Trait { NONE, IRON_STOMACH, LACTOSE_INTOLERANT, VEGETARIAN }

    public PoopManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.KEY_SATIETY = new NamespacedKey(plugin, "satiety");
        this.KEY_POOP_LEVEL = new NamespacedKey(plugin, "poop_level");
        this.KEY_HAS_EATEN_GOLD = new NamespacedKey(plugin, "has_eaten_gold");
        this.KEY_IS_POOP_ITEM = new NamespacedKey(plugin, "is_poop_item");
        this.KEY_TRAIT = new NamespacedKey(plugin, "trait");
        this.KEY_STENCH_TIME = new NamespacedKey(plugin, "stench_time");
    }

    public double getSatiety(Player p) { return p.getPersistentDataContainer().getOrDefault(KEY_SATIETY, PersistentDataType.DOUBLE, 0.0); }
    public void setSatiety(Player p, double val) { p.getPersistentDataContainer().set(KEY_SATIETY, PersistentDataType.DOUBLE, Math.min(100.0, Math.max(0, val))); }
    
    public double getPoopLevel(Player p) { return p.getPersistentDataContainer().getOrDefault(KEY_POOP_LEVEL, PersistentDataType.DOUBLE, 0.0); }
    public void setPoopLevel(Player p, double val) { p.getPersistentDataContainer().set(KEY_POOP_LEVEL, PersistentDataType.DOUBLE, Math.min(100.0, Math.max(0, val))); }
    
    public void markGoldenEaten(Player p, boolean eaten) {
        if (eaten) p.getPersistentDataContainer().set(KEY_HAS_EATEN_GOLD, PersistentDataType.BYTE, (byte) 1);
        else p.getPersistentDataContainer().remove(KEY_HAS_EATEN_GOLD);
    }
    public boolean hasEatenGold(Player p) { return p.getPersistentDataContainer().has(KEY_HAS_EATEN_GOLD, PersistentDataType.BYTE); }

    public Trait getTrait(Player p) {
        if (!config.enableTraits) return Trait.NONE;
        String t = p.getPersistentDataContainer().get(KEY_TRAIT, PersistentDataType.STRING);
        if (t == null) {
            Trait[] traits = Trait.values();
            Trait newTrait = traits[new Random().nextInt(traits.length)];
            setTrait(p, newTrait);
            // 修复点：这里也需要序列化
            p.sendMessage(config.getMessage("trait-assigned", "{trait}", getTraitName(newTrait)));
            return newTrait;
        }
        return Trait.valueOf(t);
    }
    public void setTrait(Player p, Trait t) { p.getPersistentDataContainer().set(KEY_TRAIT, PersistentDataType.STRING, t.name()); }
    
    // --- 关键修复：返回序列化后的字符串，而不是 Java 对象代码 ---
    public String getTraitName(Trait t) {
        Component c = switch (t) {
            case IRON_STOMACH -> config.getMessage("trait-iron");
            case LACTOSE_INTOLERANT -> config.getMessage("trait-lactose");
            case VEGETARIAN -> config.getMessage("trait-veg");
            default -> config.getMessage("trait-none");
        };
        // 使用 MiniMessage 将组件转换回文本格式，避免乱码
        return MiniMessage.miniMessage().serialize(c);
    }

    public int getStenchTime(Player p) { return p.getPersistentDataContainer().getOrDefault(KEY_STENCH_TIME, PersistentDataType.INTEGER, 0); }
    public void setStenchTime(Player p, int seconds) {
        if (!config.enableStench) return;
        p.getPersistentDataContainer().set(KEY_STENCH_TIME, PersistentDataType.INTEGER, seconds);
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
            
            if (config.explosionDamageEntities) {
                player.getWorld().createExplosion(player.getLocation(), config.explosionPower, false, config.explosionDamageBlocks);
            } else {
                player.getWorld().createExplosion(player.getLocation(), 0.0F, false); 
            }

            player.sendMessage(config.getMessage("poop-explode"));
            
            if (config.enableStench) {
                setStenchTime(player, config.stenchDuration);
                player.sendMessage(config.getMessage("stench-start"));
            }
        } else {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 0.5f);
            player.sendMessage(config.getMessage("poop-success"));
        }

        dropPoopItem(player.getLocation(), isGold);
    }

    public void dropPoopItem(Location loc, boolean isGold) {
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
        loc.getWorld().dropItemNaturally(loc, poopItem);
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
            
            boolean collected = false;
            if (config.enableSepticTank) {
                Block below = targetBlock.getRelative(0, -1, 0);
                if (below.getState() instanceof Container container) { 
                    ItemStack poop = new ItemStack(Material.COOKED_BEEF);
                    ItemMeta meta = poop.getItemMeta();
                    meta.displayName(config.getMessage("item-poop-name"));
                    meta.getPersistentDataContainer().set(KEY_IS_POOP_ITEM, PersistentDataType.STRING, "NORMAL");
                    poop.setItemMeta(meta);
                    
                    if (container.getInventory().addItem(poop).isEmpty()) {
                         collected = true;
                         player.sendMessage(config.getMessage("toilet-collect"));
                    }
                }
            }
            
            if (!collected && Math.random() < 0.3) {
                dropPoopItem(targetBlock.getLocation().add(0.5, 1, 0.5), false);
            }
            
            player.sendMessage(config.getMessage("toilet-use"));
            return true;
        }
        return false;
    }
}
