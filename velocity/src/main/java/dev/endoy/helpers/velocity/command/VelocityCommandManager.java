package dev.endoy.helpers.velocity.command;

import com.velocitypowered.api.proxy.ProxyServer;
import dev.endoy.helpers.common.command.CommandManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.util.List;

@RequiredArgsConstructor
public class VelocityCommandManager implements CommandManager<VelocityCommand, VelocityTabComplete>
{

    private final ProxyServer proxyServer;

    @Override
    @SneakyThrows
    public void registerCommand( String command,
                                 List<String> aliases,
                                 String permission,
                                 VelocityCommand simpleCommand,
                                 @Nullable VelocityTabComplete tabComplete,
                                 boolean override )
    {
        CommandHolder commandHolder = new CommandHolder( simpleCommand, tabComplete );

        if ( override )
        {
            proxyServer.getCommandManager().unregister( command );

            aliases.forEach( alias -> proxyServer.getCommandManager().unregister( alias ) );
        }

        proxyServer.getCommandManager().register(
            proxyServer.getCommandManager()
                .metaBuilder( command )
                .aliases( aliases.toArray( String[]::new ) )
                .build(),
            commandHolder
        );
    }
}
