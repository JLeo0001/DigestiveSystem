package com.example.digestivesystem.task;

import com.example.digestivesystem.manager.ConfigManager;
import com.example.digestivesystem.manager.PoopManager;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;

public class DigestTask extends BukkitRunnable {
    private final PoopManager manager;
    private final ConfigManager config;

    public DigestTask(PoopManager manager, ConfigManager config) {
        this.manager = manager;
        this.config = config;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double satiety = manager.getSatiety(player);
            if (satiety > 0) {
                double digestSpeed = config.digestSpeed;
                double amount = Math.min(satiety, digestSpeed);
                manager.setSatiety(player, satiety - amount);
                manager.setPoopLevel(player, manager.getPoopLevel(player) + amount);
            }
            double poopLevel = manager.getPoopLevel(player);

            if (poopLevel >= 100) {
                manager.poop(player, true);
            } else if (poopLevel > 90) {
                Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(500));
                Title title = Title.title(config.getMessage("title-warn-main"), config.getMessage("title-warn-sub"), times);
                player.showTitle(title);
                player.sendActionBar(config.getMessage("actionbar-critical", "{amount}", String.format("%.1f", poopLevel)));
            } else if (poopLevel > 70) {
                if (poopLevel % 5 < 1) player.sendActionBar(config.getMessage("actionbar-warn"));
            }
        }
    }
}
