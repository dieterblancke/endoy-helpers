package dev.endoy.helpers.spigot.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.List;

public class CommandHolder extends Command
{

    private final SpigotCommand spigotCommand;
    private final SpigotTabComplete tabComplete;

    public CommandHolder( String command,
                          List<String> aliases,
                          SpigotCommand spigotCommand,
                          SpigotTabComplete tabComplete )
    {
        super( command, "", "", aliases );

        this.spigotCommand = spigotCommand;
        this.tabComplete = tabComplete;
    }

    @Override
    public boolean execute( @Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] args )
    {
        spigotCommand.onCommand( sender, List.of( args ) );
        return true;
    }

    @Override
    public List<String> tabComplete( @Nonnull CommandSender sender, @Nonnull String alias, @Nonnull String[] args ) throws IllegalArgumentException
    {
        if ( tabComplete == null )
        {
            return super.tabComplete( sender, alias, args );
        }
        else
        {
            return tabComplete.onTabComplete( sender, List.of( args ) );
        }
    }
}
