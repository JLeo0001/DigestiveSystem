package com.example.digestivesystem.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class StatsManager {
    private final JavaPlugin plugin;
    private final File statsFile;
    private FileConfiguration statsConfig;

    // 缓存数据: UUID -> {Name, PoopCount, ExplodeCount}
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();

    public static class PlayerStats {
        public String name;
        public int poopCount;
        public int explodeCount;

        public PlayerStats(String name, int poopCount, int explodeCount) {
            this.name = name;
            this.poopCount = poopCount;
            this.explodeCount = explodeCount;
        }
    }

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");
        reloadStats();
    }

    public void reloadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        statsCache.clear();

        for (String key : statsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String name = statsConfig.getString(key + ".name");
                int poop = statsConfig.getInt(key + ".poop", 0);
                int explode = statsConfig.getInt(key + ".explode", 0);
                statsCache.put(uuid, new PlayerStats(name, poop, explode));
            } catch (Exception ignored) {}
        }
    }

    public void saveStats() {
        for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
            String path = entry.getKey().toString();
            statsConfig.set(path + ".name", entry.getValue().name);
            statsConfig.set(path + ".poop", entry.getValue().poopCount);
            statsConfig.set(path + ".explode", entry.getValue().explodeCount);
        }
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 增加数据
    public void addPoop(Player player) {
        PlayerStats stats = getOrCreate(player);
        stats.poopCount++;
        saveStatsAsync();
    }

    public void addExplosion(Player player) {
        PlayerStats stats = getOrCreate(player);
        stats.explodeCount++;
        saveStatsAsync();
    }

    private PlayerStats getOrCreate(Player player) {
        if (!statsCache.containsKey(player.getUniqueId())) {
            statsCache.put(player.getUniqueId(), new PlayerStats(player.getName(), 0, 0));
        }
        // 更新名字(万一改名了)
        PlayerStats stats = statsCache.get(player.getUniqueId());
        stats.name = player.getName();
        return stats;
    }

    private void saveStatsAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveStats);
    }

    // 获取前N名 (type: 0=poop, 1=explode)
    public List<PlayerStats> getTopPlayers(int type, int limit) {
        return statsCache.values().stream()
                .sorted((a, b) -> {
                    if (type == 0) return Integer.compare(b.poopCount, a.poopCount);
                    else return Integer.compare(b.explodeCount, a.explodeCount);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
}
