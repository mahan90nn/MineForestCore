package in.mineforest.core.paper;

import in.mineforest.core.commons.Messages;
import in.mineforest.core.commons.messaging.MessagingConstants;
import in.mineforest.core.commons.messaging.PacketFactory;
import in.mineforest.core.commons.messaging.impl.CommandForwardPacket;
import in.mineforest.core.commons.messaging.impl.HandshakePacket;
import in.mineforest.core.commons.messaging.impl.NickUpdatePacket;
import in.mineforest.core.paper.command.ForwardCommand;
import in.mineforest.core.paper.listener.ConnectionListener;
import in.mineforest.core.paper.listener.LPCListener;
import in.mineforest.core.paper.listener.StatsListener;
import net.kyori.adventure.text.TextReplacementConfig;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class PaperEngine extends JavaPlugin implements Listener {
	public static PaperEngine INSTANCE;
	public static FileConfiguration CONFIG;
	public static StatsSave STATS;

	public static LuckPerms LUCKPERMS;

	public static PacketFactory PACKET_FACTORY;
	public static Server SERVER;

	public static HashMap<Player, String> NICKS;

	@Override
	public void onEnable() {
		INSTANCE = this;
		CONFIG = getConfig();
		STATS = new StatsSave(getDataFolder());
		LUCKPERMS = getServer().getServicesManager().load(LuckPerms.class);
		SERVER = getServer();

		NICKS = new HashMap<>();

		saveDefaultConfig();
		initializePacketFactory();

		new ConnectionListener(this, SERVER, getLogger());
		new LPCListener(this, SERVER, getLogger());
		new StatsListener(this, SERVER, getLogger());

		SERVER.getPluginCommand("forward_command").setExecutor(new ForwardCommand());

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			getLogger().info("Hooking into PlaceholderAPI");
			new PapiHook().register();
        }

		getServer().getPluginManager().registerEvents(this, this);
	}

	private void initializePacketFactory() {
        PACKET_FACTORY = new PacketFactory(sender -> ((Player) sender.connection()).sendPluginMessage(this, MessagingConstants.ENGINE_CHANNEL.channelName, sender.data()));

        PACKET_FACTORY.registerPacket("handshake", HandshakePacket::new, handshakePacket -> {
			Player player = getServer().getPlayer(handshakePacket.getPlayerUUID());

			if (!player.getName().equals(handshakePacket.getPlayerNickname())) {
				player.setDisplayName("~" + handshakePacket.getPlayerNickname());
			} else {
				player.setDisplayName(player.getName());
			}

			NICKS.put(player, player.getDisplayName());

			player.sendMessage(Messages.DISPLAY_NAME_CHANGED.replace("{nickname}", handshakePacket.getPlayerNickname()));


			getServer().broadcast(
					Messages.joinMessage
							.replaceText(
									TextReplacementConfig.builder()
											.matchLiteral("{name}")
											.replacement(player.displayName())
											.build()
							)
			);
		});

        PACKET_FACTORY.registerPacket("nick_update", NickUpdatePacket::new, nickUpdatePacket -> {
			Player player = getServer().getPlayer(nickUpdatePacket.getPlayerUUID());
			try {
				NICKS.put(player, nickUpdatePacket.getPlayerNickname());
				if (!player.getName().equals(nickUpdatePacket.getPlayerNickname())) {
					player.setDisplayName("~" + nickUpdatePacket.getPlayerNickname());
				} else {
					player.setDisplayName(player.getName());
				}
				NICKS.put(player, player.getDisplayName());
				player.sendMessage(Messages.DISPLAY_NAME_CHANGED.replace("{nickname}", nickUpdatePacket.getPlayerNickname()));
			} catch (Exception e) {
				player.sendMessage(Messages.ERROR_READING_MESSAGE_NICK_UPDATE);
			}
		});

        PACKET_FACTORY.registerPacket("command_forward", CommandForwardPacket::new, commandForwardPacket -> {
            Bukkit.dispatchCommand(getServer().getPlayer(commandForwardPacket.getPlayerUUID()), commandForwardPacket.getCommand());
        });

        SERVER.getMessenger().registerIncomingPluginChannel(this, MessagingConstants.ENGINE_CHANNEL.channelName, (String channel, Player player, byte[] data) -> {
            if (!channel.equals(MessagingConstants.ENGINE_CHANNEL.channelName)) return;
            if (data == null) return;

            PACKET_FACTORY.decodeAndApply(data);
        });

		SERVER.getMessenger().registerOutgoingPluginChannel(this, "minefrost:channel");
    }
}
