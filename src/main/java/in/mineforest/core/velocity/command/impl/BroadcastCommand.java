package in.mineforest.core.velocity.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.command.VelocityCommand;
import net.kyori.adventure.text.Component;

public class BroadcastCommand extends VelocityCommand {

    public BroadcastCommand() {
        super("alert", "bc", "announce", "broadcast");
    }

    @Override
    public BrigadierCommand build() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal(commandName)
                        .requires(commandSource -> commandSource.hasPermission("velocity.command.broadcast"))
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                                .executes(commandContext -> {
                                        VelocityEngine.PROXY_SERVER.sendMessage(Component.text(commandContext.getArgument("message", String.class)));
                                    return SINGLE_SUCCESS;
                                })
                        )
        );
    }
}
