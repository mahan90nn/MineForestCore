package in.mineforest.core.paper.listener;

import in.mineforest.core.paper.PaperEngine;
import in.mineforest.core.paper.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class StatsListener extends PaperListener {
    public StatsListener(PaperEngine plugin, Server server, Logger logger) {
        super(plugin, server, logger);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        CompletableFuture.runAsync(() -> {
            if (victim.getKiller() != null && !victim.getKiller().equals(victim)) {
                Player attacker = victim.getKiller();
                int currentStreak = StatsManager.getCurrentStreak(attacker.getUniqueId());
                currentStreak++;
                StatsManager.updateKillStreak(attacker, currentStreak);
                StatsManager.updateMaxKillStreak(attacker, Math.max(currentStreak, StatsManager.getHighestStreak(attacker.getUniqueId())));
                StatsManager.addKills(attacker, 1);
            }

            StatsManager.updateKillStreak(victim, 0);
            StatsManager.updateMaxKillStreak(victim, StatsManager.getHighestStreak(victim.getUniqueId()));
            StatsManager.addDeaths(victim, 1);
        });
    }
}
