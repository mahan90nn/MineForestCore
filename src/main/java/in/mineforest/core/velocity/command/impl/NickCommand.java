package in.mineforest.core.velocity.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import in.mineforest.core.commons.Messages;
import in.mineforest.core.commons.messaging.impl.NickUpdatePacket;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.command.VelocityCommand;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;

public class NickCommand extends VelocityCommand {
    private static final Component invalidFormat = Component.text("§4§lYour nickname format is incorrect, it must must be alphanumeric!");

    public NickCommand() {
        super("nick", "nickname");
    }

    public BrigadierCommand build() {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder.<CommandSource>literal(super.commandName)
                .requires(commandSource -> commandSource.hasPermission("mineforest.core.nick"))
                .executes(commandContext -> {
                    if (!(commandContext.getSource() instanceof Player player)) {
                        commandContext.getSource().sendMessage(Messages.invalidSourceConsole);
                        return SINGLE_SUCCESS;
                    }

                    try {
                        VelocityEngine.NICK_MANAGER.resetNick(player);
                        player.getCurrentServer().ifPresent(server ->
                                VelocityEngine.PACKET_FACTORY.encodeAndSend(
                                        new NickUpdatePacket(
                                                player.getUniqueId(),
                                                player.getUsername()
                                        ),
                                        server
                                )
                        );
                    } catch (SQLException e) {
                        VelocityEngine.LOGGER.error("Failed to update player's nickname in database", e);
                        player.sendMessage(Messages.databaseError);
                    }

                    return SINGLE_SUCCESS;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word())
                        .executes(commandContext -> {
                    if (!(commandContext.getSource() instanceof Player player)) {
                        commandContext.getSource().sendMessage(Messages.invalidSourceConsole);
                        return SINGLE_SUCCESS;
                    }
                    String nick = commandContext.getArgument("name", String.class);

                    if (!nick.matches("^[a-zA-Z0-9_]{3,16}$")) {
                        player.sendMessage(invalidFormat);
                        return SINGLE_SUCCESS;
                    }
                    try {
                        VelocityEngine.NICK_MANAGER.setNick(player, nick);
                    } catch (SQLException e) {
                        VelocityEngine.LOGGER.error("Failed to update player's nickname in database", e);
                        player.sendMessage(Messages.databaseError);
                    }

                    player.getCurrentServer().ifPresent(server ->
                            VelocityEngine.PACKET_FACTORY.encodeAndSend(
                                    new NickUpdatePacket(
                                            player.getUniqueId(),
                                            nick
                                    ),
                                    server
                            )
                    );
                    return SINGLE_SUCCESS;

                })).build();

        return new BrigadierCommand(command);
    }
}
