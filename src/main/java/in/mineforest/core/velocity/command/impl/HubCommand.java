package in.mineforest.core.velocity.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import in.mineforest.core.velocity.VelocityEngine;
import in.mineforest.core.velocity.command.VelocityCommand;
import net.kyori.adventure.text.Component;

public class HubCommand extends VelocityCommand {
    public HubCommand() {
        super("hub", "lobby");
    }

    @Override
    public BrigadierCommand build() {
        return new BrigadierCommand(
                LiteralArgumentBuilder.<CommandSource>literal(commandName)
                        .executes(commandContext -> {
                            if (!(commandContext.getSource() instanceof Player player)) return SINGLE_SUCCESS;
                            player.createConnectionRequest(VelocityEngine.PROXY_SERVER.getServer("hub-1").get()).fireAndForget();
                                    return SINGLE_SUCCESS;
                        })
        );
    }
}
