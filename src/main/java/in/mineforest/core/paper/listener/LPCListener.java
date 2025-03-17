package in.mineforest.core.paper.listener;

import in.mineforest.core.paper.PaperEngine;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LPCListener extends PaperListener {
    public LPCListener(PaperEngine plugin, Server server, Logger logger) {
        super(plugin, server, logger);
    }

    @EventHandler(priority = EventPriority.MONITOR)
	public void onChat(final AsyncPlayerChatEvent event) {
		final String message = event.getMessage();
		final Player player = event.getPlayer();

		player.setDisplayName(PaperEngine.NICKS.get(event.getPlayer())); // Temporary workaround
		// some plugin is overiding it

		// Get a LuckPerms cached metadata for the player.
		final CachedMetaData metaData = PaperEngine.LUCKPERMS.getPlayerAdapter(Player.class).getMetaData(player);
		final String group = metaData.getPrimaryGroup();

		String format = PaperEngine.CONFIG.getString(PaperEngine.CONFIG.getString("group-formats." + group) != null ? "group-formats." + group : "chat-format")
				.replace("{prefix}", metaData.getPrefix() != null ? metaData.getPrefix() : "")
				.replace("{suffix}", metaData.getSuffix() != null ? metaData.getSuffix() : "")
				.replace("{prefixes}", metaData.getPrefixes().keySet().stream().map(key -> metaData.getPrefixes().get(key)).collect(Collectors.joining()))
				.replace("{suffixes}", metaData.getSuffixes().keySet().stream().map(key -> metaData.getSuffixes().get(key)).collect(Collectors.joining()))
				.replace("{world}", player.getWorld().getName())
				.replace("{name}", player.getName())
				.replace("{displayname}", player.getDisplayName())
				.replace("{username-color}", metaData.getMetaValue("username-color") != null ? metaData.getMetaValue("username-color") : "")
				.replace("{message-color}", metaData.getMetaValue("message-color") != null ? metaData.getMetaValue("message-color") : "");

		format = colorize(translateHexColorCodes(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI") ? PlaceholderAPI.setPlaceholders(player, format) : format));

		event.setFormat(format.replace("{message}", player.hasPermission("lpc.colorcodes") && player.hasPermission("lpc.rgbcodes")
				? colorize(translateHexColorCodes(message)) : player.hasPermission("lpc.colorcodes") ? colorize(message) : player.hasPermission("lpc.rgbcodes")
				? translateHexColorCodes(message) : message).replace("%", "%%"));
	}

	private String colorize(final String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	private String translateHexColorCodes(final String message) {
		final char colorChar = ChatColor.COLOR_CHAR;

		final Matcher matcher = Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(message);
		final StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);

		while (matcher.find()) {
			final String group = matcher.group(1);

			matcher.appendReplacement(buffer, colorChar + "x"
					+ colorChar + group.charAt(0) + colorChar + group.charAt(1)
					+ colorChar + group.charAt(2) + colorChar + group.charAt(3)
					+ colorChar + group.charAt(4) + colorChar + group.charAt(5));
		}

		return matcher.appendTail(buffer).toString();
	}
}
