package com.example.digestivesystem;

import com.example.digestivesystem.command.MainCommand;
import com.example.digestivesystem.gui.RankGui; // 新增
import com.example.digestivesystem.gui.StomachGui;
import com.example.digestivesystem.listener.PlayerListener;
import com.example.digestivesystem.listener.PoopInteractionListener;
import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import com.example.digestivesystem.manager.StatsManager; // 新增
import com.example.digestivesystem.task.DigestTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class DigestiveSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        StatsManager statsManager = new StatsManager(this); // 新增
        PoopManager poopManager = new PoopManager(this, configManager, statsManager); // 注入statsManager
        
        RankGui rankGui = new RankGui(statsManager, configManager); // 新增
        StomachGui gui = new StomachGui(poopManager, configManager, rankGui); // 注入rankGui

        MainCommand cmdExecutor = new MainCommand(poopManager, gui, rankGui, configManager, statsManager);
        getCommand("poop").setExecutor(cmdExecutor);
        getCommand("stomach").setExecutor(cmdExecutor);

        // PlayerListener 现在需要 StomachGui 来处理界面跳转
        getServer().getPluginManager().registerEvents(new PlayerListener(poopManager, configManager, gui), this);
        getServer().getPluginManager().registerEvents(new PoopInteractionListener(this, poopManager, configManager), this);

        new DigestTask(poopManager, configManager).runTaskTimer(this, 20L, 20L);
    }
}
