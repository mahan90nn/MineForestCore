package in.mineforest.core.paper;

import in.mineforest.core.paper.stats.StatsManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "core";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SpigotRCE";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(final OfflinePlayer player, final @NotNull String identifier) {
        String[] args = identifier.split("_");
        if (args.length == 0) return "WHAT?!";
        if (args[0].equals("maintenance")) {
            if (PlaceholderAPI.setPlaceholders(player, "%maintenance_server_" + args[1] + "%").equals("disabled"))
                return "&aOnline";
            return "&cMaintenance";
        }

        // I know I can make this better
        // but lazy right now
        // and have to go for sleep
        // Thanks future me
        // I know u r as retard as me right now
        // LMAO
        if (args[0].equals("stats")) {
            if (!args[1].equals("player")) {
                switch (args[2]) {
                    case "kills" -> {
                        return String.valueOf(StatsManager.getCurrentKills(player.getUniqueId()));
                    }
                    case "deaths" -> {
                        return String.valueOf(StatsManager.getCurrentDeaths(player.getUniqueId()));
                    }
                }
            }

            switch (args[1]) {
                case "kills" -> {
                    return String.valueOf(StatsManager.getCurrentKills(Bukkit.getOfflinePlayer(args[2]).getUniqueId()));
                }
                case "deaths" -> {
                    return String.valueOf(StatsManager.getCurrentDeaths(Bukkit.getOfflinePlayer(args[2]).getUniqueId()));
                }
            }
        }
        return null;
    }
}
