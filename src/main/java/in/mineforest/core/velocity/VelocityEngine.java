package in.mineforest.core.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import in.mineforest.core.commons.messaging.MessagingConstants;
import in.mineforest.core.commons.messaging.PacketFactory;
import in.mineforest.core.commons.messaging.impl.CommandForwardPacket;
import in.mineforest.core.commons.messaging.impl.HandshakePacket;
import in.mineforest.core.commons.messaging.impl.NickUpdatePacket;
import in.mineforest.core.velocity.command.impl.BroadcastCommand;
import in.mineforest.core.velocity.command.impl.HubCommand;
import in.mineforest.core.velocity.command.impl.NickCommand;
import in.mineforest.core.velocity.command.impl.ReportCommand;
import in.mineforest.core.velocity.database.PlayerDatabase;
import in.mineforest.core.velocity.database.ReportDatabase;
import in.mineforest.core.velocity.listener.impl.PlayerConnectionListener;
import in.mineforest.core.velocity.listener.impl.PlayerServerListener;
import in.mineforest.core.velocity.manager.NickManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "mineforest-core",
        name = "MineForest Core",
        version = "1.0.0",
        description = "A core plugin for the MineForest game."
)
public class VelocityEngine {
    public static VelocityEngine INSTANCE;

    public static PacketFactory PACKET_FACTORY;

    public static NickManager NICK_MANAGER;

    public static PlayerDatabase PLAYER_DATABASE;
    public static ReportDatabase REPORT_DATABASE;

    public static Config CONFIG;

    @Inject
    public static Logger LOGGER;
    @Inject
    @DataDirectory
    public static Path DATA_DIRECTORY;
    @Inject
    public static ProxyServer PROXY_SERVER;

    @Inject
    public VelocityEngine(Logger logger, @DataDirectory Path dataDirectory, ProxyServer proxyServer) {
        INSTANCE = this;
        LOGGER = logger;
        DATA_DIRECTORY = dataDirectory;
        PROXY_SERVER = proxyServer;

        CONFIG = new Config(DATA_DIRECTORY);
        try {
            CONFIG.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        PLAYER_DATABASE = new PlayerDatabase(LOGGER);
        REPORT_DATABASE = new ReportDatabase(LOGGER);

        NICK_MANAGER = new NickManager();
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        LOGGER.info("Initializing MineForest Core...");
        initializePacketFactory();
        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        new HubCommand();
        new BroadcastCommand();
        new NickCommand();
        new ReportCommand();
        new CommandForwardPacket();
    }

    private void registerListeners() {
        new PlayerConnectionListener(PROXY_SERVER, INSTANCE, LOGGER);
        new PlayerServerListener(PROXY_SERVER, INSTANCE, LOGGER);
    }

    private void initializePacketFactory() {
        PACKET_FACTORY = new PacketFactory(sender -> ((ServerConnection) sender.connection()).
                sendPluginMessage(MinecraftChannelIdentifier.from(MessagingConstants.ENGINE_CHANNEL.channelName), sender.data()));

        PACKET_FACTORY.registerPacket("handshake", HandshakePacket::new, handshakePacket -> {
            // not s -> p
        });

        PACKET_FACTORY.registerPacket("nick_update", NickUpdatePacket::new, nickUpdatePacket -> {
            // not s -> p
        });

        PACKET_FACTORY.registerPacket("command_forward", CommandForwardPacket::new, commandForwardPacket -> {
            PROXY_SERVER.getCommandManager().executeImmediatelyAsync(PROXY_SERVER.getPlayer(commandForwardPacket.getPlayerUUID()).get(), commandForwardPacket.getCommand());
        });

        PROXY_SERVER.getChannelRegistrar().register(MinecraftChannelIdentifier.from(MessagingConstants.ENGINE_CHANNEL.channelName));

        PROXY_SERVER.getEventManager().register(this, PluginMessageEvent.class, PostOrder.NORMAL, event -> {
            if (!event.getIdentifier().equals(MinecraftChannelIdentifier.from(MessagingConstants.ENGINE_CHANNEL.channelName)))
                return;
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            if (event.getSource() instanceof Player) return;

            PACKET_FACTORY.decodeAndApply(event.getData());
        });
    }
}
