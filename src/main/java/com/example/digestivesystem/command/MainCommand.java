package com.example.digestivesystem.command;

import com.example.digestivesystem.gui.RankGui; // 新增
import com.example.digestivesystem.gui.StomachGui;
import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import com.example.digestivesystem.manager.StatsManager; // 新增
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class MainCommand implements CommandExecutor {
    private final PoopManager poopManager;
    private final StomachGui gui;
    private final RankGui rankGui; // 新增
    private final ConfigManager config;
    private final StatsManager statsManager; // 新增

    public MainCommand(PoopManager poopManager, StomachGui gui, RankGui rankGui, ConfigManager config, StatsManager statsManager) {
        this.poopManager = poopManager;
        this.gui = gui;
        this.rankGui = rankGui;
        this.config = config;
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("poop")) {
            // 处理 /poop rank
            if (args.length > 0 && args[0].equalsIgnoreCase("rank")) {
                sendChatRank(player);
                return true;
            }

            // 处理普通 /poop
            if (poopManager.getPoopLevel(player) < 20) {
                player.sendMessage(config.getMessage("poop-fail"));
            } else {
                poopManager.poop(player, false);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("stomach")) {
            // 处理 /stomach rank
            if (args.length > 0 && args[0].equalsIgnoreCase("rank")) {
                rankGui.openGui(player);
                return true;
            }
            gui.openGui(player);
            return true;
        }
        return false;
    }

    private void sendChatRank(Player player) {
        player.sendMessage(config.getMessage("rank-chat-header"));
        
        // 顺畅榜
        player.sendMessage(config.getMessage("rank-chat-poop"));
        List<StatsManager.PlayerStats> topPoopers = statsManager.getTopPlayers(0, 10);
        for (int i = 0; i < topPoopers.size(); i++) {
            StatsManager.PlayerStats ps = topPoopers.get(i);
            player.sendMessage(config.getMessage("rank-chat-entry", 
                    "{rank}", String.valueOf(i+1),
                    "{player}", ps.name,
                    "{count}", String.valueOf(ps.poopCount)));
        }
        
        player.sendMessage(Component.empty());

        // 喷射榜
        player.sendMessage(config.getMessage("rank-chat-explode"));
        List<StatsManager.PlayerStats> topExploders = statsManager.getTopPlayers(1, 10);
        for (int i = 0; i < topExploders.size(); i++) {
            StatsManager.PlayerStats ps = topExploders.get(i);
            player.sendMessage(config.getMessage("rank-chat-entry", 
                    "{rank}", String.valueOf(i+1),
                    "{player}", ps.name,
                    "{count}", String.valueOf(ps.explodeCount)));
        }
    }
}
