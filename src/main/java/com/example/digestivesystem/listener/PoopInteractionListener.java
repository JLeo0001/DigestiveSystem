package com.example.digestivesystem.listener;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PoopInteractionListener implements Listener {
    private final JavaPlugin plugin;
    private final PoopManager poopManager;
    private final ConfigManager config;

    public PoopInteractionListener(JavaPlugin plugin, PoopManager poopManager, ConfigManager config) {
        this.plugin = plugin;
        this.poopManager = poopManager;
        this.config = config;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CAULDRON) {
            if (poopManager.getPoopLevel(player) > 50) {
                event.setCancelled(true);
                if (player.isSneaking()) poopManager.tryToiletPoop(player);
                else player.sendMessage(config.getMessage("toilet-fail"));
                return;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        String poopType = poopManager.getPoopType(item);
        if (poopType == null) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            if (block.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    ageable.setAge(ageable.getMaximumAge());
                    block.setBlockData(ageable);
                    // 修正点：VILLAGER_HAPPY -> HAPPY_VILLAGER
                    block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 10);
                    block.getWorld().playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1f, 1f);
                    item.subtract(1);
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Snowball poopProjectile = player.launchProjectile(Snowball.class);
            poopProjectile.setItem(item);
            poopProjectile.setMetadata("is_poop_ball", new FixedMetadataValue(plugin, true));
            if (poopType.equals("GOLD")) poopProjectile.setMetadata("is_gold_poop", new FixedMetadataValue(plugin, true));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 0.5f);
            if (!player.getGameMode().equals(GameMode.CREATIVE)) item.subtract(1);
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!snowball.hasMetadata("is_poop_ball")) return;

        if (event.getHitEntity() instanceof LivingEntity target) {
            boolean isGold = snowball.hasMetadata("is_gold_poop");
            if (isGold) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 2));
                target.sendMessage(Component.text("被金子砸中的感觉不错吧。", NamedTextColor.GOLD));
            } else {
                target.damage(2.0);
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 2));
                target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 1));
                if (target instanceof Player targetP) {
                    targetP.sendTitlePart(net.kyori.adventure.title.TitlePart.TITLE, Component.text("💩", NamedTextColor.YELLOW));
                }
            }
        }
        snowball.getWorld().playSound(snowball.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 1f, 0.8f);
        snowball.getWorld().spawnParticle(Particle.ITEM_SLIME, snowball.getLocation(), 20, 0.2, 0.2, 0.2);
    }
}
