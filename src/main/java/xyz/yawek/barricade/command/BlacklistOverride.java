package xyz.yawek.barricade.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.yawek.barricade.Barricade;

import java.util.ArrayList;

public class BlacklistOverride implements SimpleCommand {

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        // Get the arguments after the command alias
        String command = "barricade blacklist";
        String[] args = invocation.arguments();
        for (int i = 0; i < args.length; i++) {
            command = command + " " + args[i];
        }

        Barricade.getBarricade().getServer().getCommandManager().executeImmediatelyAsync(source, command);
    }

}
