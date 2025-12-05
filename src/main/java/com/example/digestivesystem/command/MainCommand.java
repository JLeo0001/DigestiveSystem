package com.example.digestivesystem.command;

import com.example.digestivesystem.gui.StomachGui;
import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {
    private final PoopManager poopManager;
    private final StomachGui gui;
    private final ConfigManager config;

    public MainCommand(PoopManager poopManager, StomachGui gui, ConfigManager config) {
        this.poopManager = poopManager;
        this.gui = gui;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (command.getName().equalsIgnoreCase("poop")) {
            if (poopManager.getPoopLevel(player) < 20) {
                player.sendMessage(config.getMessage("poop-fail"));
            } else {
                poopManager.poop(player, false);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("stomach")) {
            gui.openGui(player);
            return true;
        }
        return false;
    }
}
