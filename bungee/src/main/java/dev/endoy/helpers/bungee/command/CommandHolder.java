package dev.endoy.helpers.bungee.command;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.List;

public class CommandHolder extends Command implements TabExecutor
{

    private final BungeeCommand bungeeCommand;
    private final BungeeTabComplete tabComplete;

    public CommandHolder( String command,
                          List<String> aliases,
                          String permission,
                          BungeeCommand bungeeCommand,
                          BungeeTabComplete tabComplete )
    {
        super( command, permission, aliases.toArray( String[]::new ) );

        this.bungeeCommand = bungeeCommand;
        this.tabComplete = tabComplete;
    }

    @Override
    public void execute( CommandSender sender, String[] args )
    {
        this.bungeeCommand.onCommand( sender, List.of( args ) );
    }

    @Override
    public Iterable<String> onTabComplete( CommandSender sender, String[] args )
    {
        if ( this.tabComplete == null )
        {
            return ImmutableList.of();
        }
        return this.tabComplete.onTabComplete( sender, List.of( args ) );
    }
}
