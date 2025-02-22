package dev.endoy.helpers.bungee.command;

import dev.endoy.helpers.common.command.CommandManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class BungeeCommandManager implements CommandManager<BungeeCommand, BungeeTabComplete>
{

    private final Plugin plugin;

    @Override
    @SneakyThrows
    public void registerCommand( String command,
                                 List<String> aliases,
                                 String permission,
                                 BungeeCommand simpleCommand,
                                 @Nullable BungeeTabComplete tabComplete,
                                 boolean override )
    {
        CommandHolder commandHolder = new CommandHolder(
            command,
            aliases,
            permission,
            simpleCommand,
            tabComplete
        );

        ProxyServer.getInstance().getPluginManager().registerCommand( plugin, commandHolder );
    }
}
