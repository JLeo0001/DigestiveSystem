package com.example.digestivesystem.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<Material, Double> foodValues = new HashMap<>();
    public double digestSpeed;
    public boolean explodeDamage;
    public Material specialTriggerFood;
    private final Map<String, String> messageCache = new HashMap<>();
    private FileConfiguration langConfig;

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
        for (String key : langConfig.getKeys(true)) {
            if (langConfig.isString(key)) messageCache.put(key, langConfig.getString(key));
        }
    }

    private void saveDefaultLanguage(String langName) {
        File file = new File(plugin.getDataFolder(), "lang/" + langName + ".yml");
        if (!file.exists()) plugin.saveResource("lang/" + langName + ".yml", false);
    }

    public double getFoodValue(Material material) {
        return foodValues.getOrDefault(material, material.isEdible() ? 5.0 : 0.0);
    }

    public Component getMessage(String key, String... placeholders) {
        String rawMsg = messageCache.getOrDefault(key, "<red>Missing string: " + key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) rawMsg = rawMsg.replace(placeholders[i], placeholders[i + 1]);
        }
        String prefix = messageCache.getOrDefault("prefix", "");
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
