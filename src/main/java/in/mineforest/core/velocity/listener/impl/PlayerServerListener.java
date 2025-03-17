package in.mineforest.core.velocity.listener.impl;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import in.mineforest.core.commons.Messages;
import in.mineforest.core.commons.messaging.impl.HandshakePacket;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.database.PlayerDatabase;
import in.mineforest.core.velocity.listener.VelocityListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.slf4j.Logger;

import java.sql.SQLException;

public class PlayerServerListener extends VelocityListener {

    public PlayerServerListener(ProxyServer proxyServer, VelocityEngine plugin, Logger logger) {
        super(proxyServer, plugin, logger);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        PlayerDatabase.PlayerModel playerModel = null;
        try {
            playerModel = VelocityEngine.PLAYER_DATABASE.getPlayer(event.getPlayer().getUniqueId().toString());
        } catch (Exception e) {
            event.getPlayer().sendMessage(Component.text("Unable to load your information form the database"));
            return;
        }

        playerModel.serverName = event.getServer().getServerInfo().getName();

        try {
            VelocityEngine.PLAYER_DATABASE.updatePlayer(playerModel);
        } catch (SQLException e) {
            getLogger().error("Failed to update player in database", e);
            event.getPlayer().sendMessage(Messages.databaseError);
        }
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(server ->
                VelocityEngine.PACKET_FACTORY.encodeAndSend(
                        new HandshakePacket(
                                event.getPlayer().getUniqueId(),
                                server.getServerInfo().getName(),
                                event.getPlayer().getUsername()
                        ),
                        server
                )
        );
    }
}
