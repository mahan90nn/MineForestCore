package in.mineforest.core.paper.listener;

import in.mineforest.core.commons.Messages;
import in.mineforest.core.paper.PaperEngine;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Logger;

public class ConnectionListener extends PaperListener {
    public ConnectionListener(PaperEngine plugin, Server server, Logger logger) {
        super(plugin, server, logger);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(Messages.quitMessage.
                replaceText(TextReplacementConfig
                        .builder()
                        .matchLiteral("{name}")
                        .replacement(
                                event.getPlayer().displayName()
                        )
                        .build()
                )
        );
    }
}
