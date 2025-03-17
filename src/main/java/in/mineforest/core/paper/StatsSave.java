package in.mineforest.core.paper;

import in.mineforest.core.commons.ConfigProvider;
import in.mineforest.core.paper.stats.StatsManager;

import java.io.File;
import java.io.IOException;

public class StatsSave extends ConfigProvider {
    public StatsSave(File dataDirectory) {
        super("stats.yml", "file-version", dataDirectory);
    }

    public StatsManager.PlayerStats getStats(String uuid) {
        StatsManager.PlayerStats stats = new StatsManager.PlayerStats();
        stats.kills = getFileConfig().getInt("players." + uuid + ".kills");
        stats.deaths = getFileConfig().getInt("players." + uuid + ".deaths");
        stats.killStreak = getFileConfig().getInt("players." + uuid + ".kill_streak");
        stats.maxKillStreak = getFileConfig().getInt("players." + uuid + ".max_kill_streak");
        return stats;
    }

    public void saveStats(String uuid, StatsManager.PlayerStats stats) throws IOException {
        getFileConfig().set("players." + uuid + ".kills", stats.kills);
        getFileConfig().set("players." + uuid + ".deaths", stats.deaths);
        getFileConfig().set("players." + uuid + ".kill_streak", stats.killStreak);
        getFileConfig().set("players." + uuid + ".max_kill_streak", stats.maxKillStreak);
        save();
    }
}
