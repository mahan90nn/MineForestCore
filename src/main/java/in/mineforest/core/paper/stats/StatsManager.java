package in.mineforest.core.paper.stats;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import in.mineforest.core.paper.PaperEngine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatsManager {
    private static final ConcurrentMap<UUID, StatsManager.PlayerStats> cache = new ConcurrentHashMap<>();

    public static void addKills(Player player, int killsToAdd) {
        UUID uuid = player.getUniqueId();
        StatsManager.PlayerStats stats = cache.computeIfAbsent(uuid, StatsManager::load);
        stats.kills += killsToAdd;
        try {
            save(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void addDeaths(Player player, int deathsToAdd) {
        UUID uuid = player.getUniqueId();
        StatsManager.PlayerStats stats = cache.computeIfAbsent(uuid, StatsManager::load);
        stats.deaths += deathsToAdd;
        try {
            save(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void updateKillStreak(Player player, int newKillStreak) {
        UUID uuid = player.getUniqueId();
        StatsManager.PlayerStats stats = cache.computeIfAbsent(uuid, StatsManager::load);
        stats.killStreak = newKillStreak;
        if (newKillStreak > stats.maxKillStreak) {
            stats.maxKillStreak = newKillStreak;
        }
        try {
            save(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void updateMaxKillStreak(Player player, int newMaxKillStreak) {
        UUID uuid = player.getUniqueId();
        cache.computeIfAbsent(uuid, StatsManager::load).maxKillStreak = newMaxKillStreak;
        try {
            save(player.getUniqueId());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static int getCurrentKills(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).kills;
    }

    public static int getCurrentDeaths(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).deaths;
    }

    public static double calculateKDR(UUID playerUUID) {
        StatsManager.PlayerStats stats = cache.computeIfAbsent(playerUUID, StatsManager::load);
        return stats.deaths == 0 ? (double) stats.kills : Double.parseDouble(String.format("%.2f", (double) stats.kills / (double) stats.deaths));
    }

    public static int getCurrentStreak(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).killStreak;
    }

    public static int getHighestStreak(UUID playerUUID) {
        return cache.computeIfAbsent(playerUUID, StatsManager::load).maxKillStreak;
    }

    public static void editKills(UUID playerUUID, int newKills) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).kills = newKills;
        try {
            save(playerUUID);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void editDeaths(UUID playerUUID, int newDeaths) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).deaths = newDeaths;
        try {
            save(playerUUID);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void editStreak(UUID playerUUID, int newStreak) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).killStreak = newStreak;
        try {
            save(playerUUID);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void editHighestStreak(UUID playerUUID, int newHighestStreak) {
        cache.computeIfAbsent(playerUUID, StatsManager::load).maxKillStreak = newHighestStreak;
        try {
            save(playerUUID);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public static void save(UUID playerUUID) throws IOException {
        StatsManager.PlayerStats stats = cache.get(playerUUID);
        if (stats == null) return;

        PaperEngine.STATS.saveStats(playerUUID.toString(), stats);
    }

    public static void saveAll() {
        for (UUID uuid : cache.keySet()) {
            try {
                save(uuid);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to save player stats: " + e.getMessage());
            }
        }
    }

    public static StatsManager.PlayerStats load(UUID playerUUID) {
        return PaperEngine.STATS.getStats(playerUUID.toString());
    }

    public static class PlayerStats {
        public int kills = 0;
        public int deaths = 0;
        public int killStreak = 0;
        public int maxKillStreak = 0;

    }
}