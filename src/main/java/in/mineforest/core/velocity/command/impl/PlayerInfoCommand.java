package in.mineforest.core.velocity.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.command.VelocityCommand;
import net.kyori.adventure.text.Component;

public class PlayerInfoCommand extends VelocityCommand {

    public PlayerInfoCommand() {
        super("playerinfo", "pi", "player");
    }

    @Override
    public BrigadierCommand build() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal(commandName)
                        .then(RequiredArgumentBuilder.<CommandSource, String>argument("identifier", StringArgumentType.word())
                                .executes(commandContext -> {
                                    String identifier = commandContext.getArgument("identifier", String.class);

                                    return SINGLE_SUCCESS;
                                })
                        )
        );
    }
}
