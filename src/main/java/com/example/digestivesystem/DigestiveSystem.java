package com.example.digestivesystem;

import com.example.digestivesystem.command.MainCommand;
import com.example.digestivesystem.gui.StomachGui;
import com.example.digestivesystem.listener.PlayerListener;
import com.example.digestivesystem.listener.PoopInteractionListener;
import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import com.example.digestivesystem.task.DigestTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class DigestiveSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        ConfigManager configManager = new ConfigManager(this);
        PoopManager poopManager = new PoopManager(this, configManager);
        StomachGui gui = new StomachGui(poopManager, configManager);

        MainCommand cmdExecutor = new MainCommand(poopManager, gui, configManager);
        getCommand("poop").setExecutor(cmdExecutor);
        getCommand("stomach").setExecutor(cmdExecutor);

        getServer().getPluginManager().registerEvents(new PlayerListener(poopManager, configManager), this);
        getServer().getPluginManager().registerEvents(new PoopInteractionListener(this, poopManager, configManager), this);

        new DigestTask(poopManager, configManager).runTaskTimer(this, 20L, 20L);
    }
}
