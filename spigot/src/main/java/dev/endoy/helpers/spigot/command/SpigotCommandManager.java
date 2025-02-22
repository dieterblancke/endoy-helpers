package dev.endoy.helpers.spigot.command;

import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.logger.Logger;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SpigotCommandManager implements CommandManager<SpigotCommand, SpigotTabComplete>
{

    private static final Logger LOGGER = Logger.forClass( SpigotCommandManager.class );
    private static final Field commandMap;

    static
    {
        commandMap = ReflectionUtils.getField( Bukkit.getServer().getClass(), "commandMap" );
    }

    @Override
    @SneakyThrows
    public void registerCommand( String command,
                                 List<String> aliases,
                                 String permission,
                                 SpigotCommand simpleCommand,
                                 @Nullable SpigotTabComplete tabComplete,
                                 boolean override )
    {
        CommandMap map = (CommandMap) commandMap.get( Bukkit.getServer() );

        if ( override )
        {
            this.unregisterCommands( map, command, aliases );
        }

        map.register( command, "endoy:", new CommandHolder(
            command,
            aliases,
            simpleCommand,
            tabComplete
        ) );
    }

    private void unregisterCommands( final CommandMap map, final String command, final List<String> aliases )
    {
        try
        {
            Field field;
            if ( ReflectionUtils.hasField( map.getClass(), "knownCommands" ) )
            {
                field = ReflectionUtils.getField( map.getClass(), "knownCommands" );
            }
            else
            {
                field = ReflectionUtils.getField( map.getClass().getSuperclass(), "knownCommands" );
            }

            Map<String, Command> commands = (Map<String, Command>) field.get( map );

            commands.remove( command );

            for ( String alias : aliases )
            {
                commands.remove( alias );
            }

            field.set( map, commands );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Failed to unregister commands: " + command + " and aliases: " + aliases );
        }
    }
}
