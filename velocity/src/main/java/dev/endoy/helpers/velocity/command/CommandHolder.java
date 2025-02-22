package dev.endoy.helpers.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CommandHolder implements SimpleCommand
{

    private final VelocityCommand velocityCommand;
    private final VelocityTabComplete tabComplete;

    @Override
    public void execute( Invocation invocation )
    {
        this.velocityCommand.onCommand( invocation.source(), List.of( invocation.arguments() ) );
    }

    @Override
    public List<String> suggest( Invocation invocation )
    {
        return this.tabComplete.onTabComplete( invocation.source(), List.of( invocation.arguments() ) );
    }
}
