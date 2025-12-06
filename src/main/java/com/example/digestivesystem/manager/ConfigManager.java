package com.example.digestivesystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<Material, Double> foodValues = new HashMap<>();
    
    // 我们将 messageCache 修改为存储 Object，可能是 String，也可能是 List<String>
    private final Map<String, Object> messageCache = new HashMap<>();
    private FileConfiguration langConfig;

    // 基础设置
    public double digestSpeed;
    public boolean explodeDamage;
    public Material specialTriggerFood;
    
    // 功能开关
    public boolean enableTraits;
    public boolean enableStench;
    public boolean stenchRefuseTrade;
    public boolean stenchAggroMobs;
    public boolean enableSlip;
    public boolean slipSound;
    public boolean enableSepticTank;

    // 平衡数值
    public int stenchDuration;
    public double lactosePenalty;
    public double vegetarianPenalty;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        digestSpeed = config.getDouble("settings.digest-speed", 0.5);
        explodeDamage = config.getBoolean("settings.explode-damage", false);
        String langCode = config.getString("settings.language", "zh_cn");

        // 加载功能开关
        enableTraits = config.getBoolean("features.enable-traits", true);
        enableStench = config.getBoolean("features.enable-stench", true);
        stenchRefuseTrade = config.getBoolean("features.stench-refuse-trade", true);
        stenchAggroMobs = config.getBoolean("features.stench-aggro-mobs", true);
        enableSlip = config.getBoolean("features.enable-slip", true);
        slipSound = config.getBoolean("features.slip-sound", true);
        enableSepticTank = config.getBoolean("features.enable-septic-tank", true);

        // 加载平衡
        stenchDuration = config.getInt("balance.stench-duration", 60);
        lactosePenalty = config.getDouble("balance.lactose-penalty", 40.0);
        vegetarianPenalty = config.getDouble("balance.vegetarian-penalty", 2.0);

        foodValues.clear();
        if (config.getConfigurationSection("foods") != null) {
            for (String key : config.getConfigurationSection("foods").getKeys(false)) {
                try {
                    foodValues.put(Material.valueOf(key), config.getDouble("foods." + key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        try {
            specialTriggerFood = Material.valueOf(config.getString("special-poops.gold.trigger-food", "GOLDEN_APPLE"));
        } catch (Exception e) { specialTriggerFood = Material.GOLDEN_APPLE; }

        loadLanguageFile(langCode);
    }

    private void loadLanguageFile(String langCode) {
        saveDefaultLanguage("zh_cn");
        saveDefaultLanguage("en_us");
        File langFile = new File(plugin.getDataFolder(), "lang/" + langCode + ".yml");
        if (!langFile.exists()) langFile = new File(plugin.getDataFolder(), "lang/zh_cn.yml");

        this.langConfig = YamlConfiguration.loadConfiguration(langFile);
        messageCache.clear();
        
        // 智能加载：如果是 List 就存 List，是 String 就存 String
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isList(key)) {
                messageCache.put(key, langConfig.getStringList(key));
            } else if (langConfig.isString(key)) {
                messageCache.put(key, langConfig.getString(key));
            }
        }
    }

    private void saveDefaultLanguage(String langName) {
        File file = new File(plugin.getDataFolder(), "lang/" + langName + ".yml");
        if (!file.exists()) plugin.saveResource("lang/" + langName + ".yml", false);
    }

    public double getFoodValue(Material material) {
        return foodValues.getOrDefault(material, 0.0);
    }

    /**
     * 核心升级：支持随机消息池
     */
    public Component getMessage(String key, String... placeholders) {
        Object rawObj = messageCache.get(key);
        String rawMsg;

        if (rawObj instanceof List) {
            // 如果是列表，随机抽取一条
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) rawObj;
            if (list.isEmpty()) rawMsg = "<red>Empty list: " + key;
            else rawMsg = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        } else if (rawObj instanceof String) {
            rawMsg = (String) rawObj;
        } else {
            rawMsg = "<red>Missing: " + key;
        }

        // 替换占位符
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) rawMsg = rawMsg.replace(placeholders[i], placeholders[i + 1]);
        }
        
        // 自动加前缀 (如果需要)
        String prefix = "";
        if (messageCache.get("prefix") instanceof String) {
            prefix = (String) messageCache.get("prefix");
        }
        
        if (!key.startsWith("item-") && !key.startsWith("gui-") && !key.startsWith("title-") && !key.startsWith("actionbar-")) {
             rawMsg = prefix + rawMsg;
        }
        
        return MiniMessage.miniMessage().deserialize(rawMsg);
    }
    
    public List<Component> getMessageList(String key) {
        List<String> rawList = langConfig.getStringList(key);
        if (rawList.isEmpty()) return Collections.emptyList();
        return rawList.stream().map(s -> MiniMessage.miniMessage().deserialize(s)).toList();
    }
}
