package in.mineforest.core.paper.command;

import in.mineforest.core.commons.messaging.impl.CommandForwardPacket;
import in.mineforest.core.paper.PaperEngine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ForwardCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player player) {
            PaperEngine.PACKET_FACTORY.encodeAndSend(new CommandForwardPacket(player.getUniqueId(), String.join(" ", strings)), player);
            return true;
        }
        commandSender.sendMessage("Only players can use this command.");
        return true;
    }
}
