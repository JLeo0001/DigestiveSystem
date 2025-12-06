package com.example.digestivesystem.gui;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.StatsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class RankGui {
    private final StatsManager statsManager;
    private final ConfigManager config;

    public RankGui(StatsManager statsManager, ConfigManager config) {
        this.statsManager = statsManager;
        this.config = config;
    }

    public void openGui(Player player) {
        // 27格界面
        Inventory inv = Bukkit.createInventory(null, 27, config.getMessage("rank-gui-title"));

        // 1. 获取数据
        List<StatsManager.PlayerStats> topPoopers = statsManager.getTopPlayers(0, 9);
        List<StatsManager.PlayerStats> topExploders = statsManager.getTopPlayers(1, 9);

        // 2. 填充背景 (灰色玻璃板)
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // 3. 渲染顺畅榜
        renderRow(inv, topPoopers, 0, "rank-poop-title", "rank-item-lore-poop");

        // 4. 渲染喷射榜
        renderRow(inv, topExploders, 18, "rank-explode-title", "rank-item-lore-explode");

        player.openInventory(inv);
    }

    private void renderRow(Inventory inv, List<StatsManager.PlayerStats> stats, int startIndex, String titleKey, String loreKey) {
        // 图标
        ItemStack icon = new ItemStack(startIndex == 0 ? Material.LIME_DYE : Material.TNT);
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.displayName(config.getMessage(titleKey));
        inv.setItem(startIndex, icon);

        // 后面8个位置
        for (int i = 0; i < 8; i++) {
            int slot = startIndex + 1 + i;
            if (i < stats.size()) {
                // 有数据：显示玩家头颅
                StatsManager.PlayerStats pStat = stats.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                
                meta.displayName(config.getMessage("rank-item-name", 
                        "{rank}", String.valueOf(i + 1), 
                        "{player}", pStat.name));
                
                String count = (startIndex == 0) ? String.valueOf(pStat.poopCount) : String.valueOf(pStat.explodeCount);
                
                List<Component> loreLines = config.getRawStringList(loreKey).stream()
                        .map(s -> config.parseMessage(s, "{count}", count))
                        .toList();
                meta.lore(loreLines);

                meta.setOwningPlayer(Bukkit.getOfflinePlayer(pStat.name));
                skull.setItemMeta(meta);
                inv.setItem(slot, skull);
            } else {
                // 修复：没数据时，不放屏障了，直接放空的灰色玻璃板（或者空气），这样看起来不乱
                // 之前是 barrier，现在什么都不做，保留背景的灰色玻璃板即可
                // 如果你想显示"虚位以待"字样，可以用浅灰色玻璃板
                ItemStack empty = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                ItemMeta emptyMeta = empty.getItemMeta();
                emptyMeta.displayName(config.getMessage("rank-empty"));
                empty.setItemMeta(emptyMeta);
                inv.setItem(slot, empty);
            }
        }
    }
}
