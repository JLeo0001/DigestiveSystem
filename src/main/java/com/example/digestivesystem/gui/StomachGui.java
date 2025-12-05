package com.example.digestivesystem.gui;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;

public class StomachGui {
    private final PoopManager manager;
    private final ConfigManager config;

    public StomachGui(PoopManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    public void openGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, config.getMessage("gui-title"));
        double satiety = manager.getSatiety(player);
        double poop = manager.getPoopLevel(player);
        String traitName = manager.getTraitName(manager.getTrait(player));

        ItemStack foodItem = new ItemStack(Material.BREAD);
        ItemMeta foodMeta = foodItem.getItemMeta();
        foodMeta.displayName(config.getMessage("gui-food-name"));
        foodMeta.lore(List.of(
            config.getMessage("status-hungry", "{amount}", String.format("%.1f", satiety)),
            config.getMessage("gui-trait-name", "{trait}", traitName),
            generateProgressBar(satiety, NamedTextColor.GREEN)
        ));
        foodItem.setItemMeta(foodMeta);

        ItemStack poopItem = new ItemStack(Material.BROWN_WOOL);
        ItemMeta poopMeta = poopItem.getItemMeta();
        poopMeta.displayName(config.getMessage("gui-poop-name"));
        poopMeta.lore(List.of(
            config.getMessage("status-poop", "{amount}", String.format("%.1f", poop)),
            generateProgressBar(poop, NamedTextColor.RED)
        ));
        poopItem.setItemMeta(poopMeta);

        inv.setItem(2, foodItem);
        inv.setItem(6, poopItem);

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.empty());
        filler.setItemMeta(fillerMeta);
        for(int i=0; i<9; i++) if(inv.getItem(i) == null) inv.setItem(i, filler);

        player.openInventory(inv);
    }

    private Component generateProgressBar(double value, NamedTextColor color) {
        int bars = (int) (value / 5);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) sb.append(i < bars ? "|" : ".");
        return Component.text("[" + sb.toString() + "]", color);
    }
}
