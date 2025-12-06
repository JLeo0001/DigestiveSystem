package com.example.digestivesystem.gui;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.StatsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class RankGui {
    private final StatsManager statsManager;
    private final ConfigManager config;

    public RankGui(StatsManager statsManager, ConfigManager config) {
        this.statsManager = statsManager;
        this.config = config;
    }

    public void openGui(Player player) {
        // 27格界面：9格顺畅榜 + 9格分隔 + 9格喷射榜
        Inventory inv = Bukkit.createInventory(null, 27, config.getMessage("rank-gui-title"));

        // 1. 获取数据
        List<StatsManager.PlayerStats> topPoopers = statsManager.getTopPlayers(0, 9);
        List<StatsManager.PlayerStats> topExploders = statsManager.getTopPlayers(1, 9);

        // 2. 填充背景
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.empty());
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // 3. 渲染顺畅榜 (第一行)
        renderRow(inv, topPoopers, 0, "rank-poop-title", "rank-item-lore-poop");

        // 4. 渲染喷射榜 (第三行)
        renderRow(inv, topExploders, 18, "rank-explode-title", "rank-item-lore-explode");

        player.openInventory(inv);
    }

    private void renderRow(Inventory inv, List<StatsManager.PlayerStats> stats, int startIndex, String titleKey, String loreKey) {
        // 在该行最左边放一个标志物
        ItemStack icon = new ItemStack(startIndex == 0 ? Material.LIME_DYE : Material.TNT);
        ItemMeta iconMeta = icon.getItemMeta();
        iconMeta.displayName(config.getMessage(titleKey));
        icon.setItemMeta(iconMeta);
        inv.setItem(startIndex, icon); // 第一个位置放标题

        // 后面8个位置放人头
        for (int i = 0; i < 8; i++) {
            int slot = startIndex + 1 + i;
            if (i < stats.size()) {
                StatsManager.PlayerStats pStat = stats.get(i);
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                
                // 设置名字
                meta.displayName(config.getMessage("rank-item-name", 
                        "{rank}", String.valueOf(i + 1), 
                        "{player}", pStat.name));
                
                // 设置Lore
                String count = (startIndex == 0) ? String.valueOf(pStat.poopCount) : String.valueOf(pStat.explodeCount);
                meta.lore(config.getMessageList(loreKey).stream().map(c -> {
                    // 简单的文本替换 (MiniMessage不支持直接替换Component中的文本，所以这里重新解析)
                    // 为了简化，我们假设 ConfigManager 已经处理好了，或者我们在 ConfigManager 里加个处理List的方法
                    // 这里我们用简便方法：先转String替换再转回Component，或者直接用 ConfigManager 的 getMessage 逻辑
                    // 由于 ConfigManager.getMessageList 返回的是 Component list，我们这里稍微hack一下
                    return c; // 暂时先不替换 Lore 里的数值，下面手动构建
                }).toList());
                
                // 手动构建带数值的 Lore，因为 ConfigManager.getMessageList 不支持变量替换
                List<Component> loreLines = config.getRawStringList(loreKey).stream()
                        .map(s -> config.parseMessage(s, "{count}", count))
                        .toList();
                meta.lore(loreLines);

                // 设置头颅皮肤
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(pStat.name));
                skull.setItemMeta(meta);
                inv.setItem(slot, skull);
            } else {
                ItemStack empty = new ItemStack(Material.BARRIER);
                ItemMeta emptyMeta = empty.getItemMeta();
                emptyMeta.displayName(config.getMessage("rank-empty"));
                empty.setItemMeta(emptyMeta);
                inv.setItem(slot, empty);
            }
        }
    }
}
