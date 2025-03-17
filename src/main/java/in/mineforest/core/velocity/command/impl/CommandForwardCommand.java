package in.mineforest.core.velocity.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import in.mineforest.core.commons.Messages;
import in.mineforest.core.commons.messaging.impl.CommandForwardPacket;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.command.VelocityCommand;
import net.kyori.adventure.text.Component;

public class CommandForwardCommand extends VelocityCommand {
    public CommandForwardCommand() {
        super("commandforwardvelocity");
    }

    @Override
    public BrigadierCommand build() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal(commandName)
                        .requires(commandSource -> commandSource.hasPermission("mineforest.admin"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("user", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String partialName;

                                    try {
                                        partialName = ctx.getArgument("user", String.class).toLowerCase();
                                    } catch (IllegalArgumentException ignored) {
                                        partialName = "";
                                    }

                                    if (partialName.isEmpty()) {
                                        VelocityEngine.PROXY_SERVER.getAllPlayers().stream().map(Player::getUsername).forEach(builder::suggest);
                                        return builder.buildFuture();
                                    }

                                    String finalPartialName = partialName;

                                    VelocityEngine.PROXY_SERVER.getAllPlayers().stream().map(Player::getUsername)
                                            .filter(
                                                    name -> name.toLowerCase().startsWith(finalPartialName)
                                            ).forEach(builder::suggest);

                                    return builder.buildFuture();

                                })
                                .executes(commandContext -> {
                                    commandContext.getSource().sendMessage(Messages.incompleteCommand);
                                    return SINGLE_SUCCESS;
                                })
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("command", StringArgumentType.greedyString())
                                        .executes(commandContext -> {
                                            Player player = VelocityEngine.PROXY_SERVER.getPlayer(commandContext.getArgument("user", String.class)).orElseThrow();
                                            String command = commandContext.getArgument("command", String.class);
                                            VelocityEngine.PACKET_FACTORY.encodeAndSend(new CommandForwardPacket(player.getUniqueId(), command), player);
                                            return SINGLE_SUCCESS;
                                        })
                                )
                        )
        );
    }
}
