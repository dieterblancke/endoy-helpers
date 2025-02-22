package dev.endoy.helpers.common.command;

import javax.annotation.Nullable;
import java.util.List;

public interface CommandManager<C, T>
{

    void registerCommand( String command,
                          List<String> aliases,
                          String permission,
                          C simpleCommand,
                          @Nullable T tabComplete,
                          boolean override );

}
