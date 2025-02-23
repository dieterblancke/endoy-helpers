package dev.endoy.helpers.common;

import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.command.SimpleCommand;
import dev.endoy.helpers.common.command.SimpleTabComplete;
import dev.endoy.helpers.common.injector.Injector;
import dev.endoy.helpers.common.task.ScheduledTask;
import dev.endoy.helpers.common.task.TaskManager;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EndoyApplicationTest extends EndoyApplication
{

    protected static File dataFolder;
    @Getter
    @Setter
    private Injector injector;

    private boolean dataFolderCreated = false;

    public EndoyApplicationTest()
    {
    }

    @AfterAll
    public static void tearDownAll()
    {
        if ( dataFolder != null )
        {
            dataFolder.delete();
            dataFolder = null;
        }
    }

    @BeforeEach
    public void setUp() throws IOException
    {
        if ( dataFolder == null )
        {
            dataFolder = TestHelper.createTempDirectory();
            dataFolderCreated = true;
        }
    }

    @AfterEach
    public void tearDown()
    {
        if ( dataFolderCreated )
        {
            dataFolder.delete();
        }
    }

    @Override
    public TaskManager getTaskManager()
    {
        return new TaskManager()
        {
            @Override
            public ScheduledTask runTask( Runnable runnable, boolean async )
            {
                return () ->
                {
                };
            }

            @Override
            public ScheduledTask runTaskLater( Runnable runnable, boolean async, long delay, TimeUnit timeUnit )
            {
                return () ->
                {
                };
            }

            @Override
            public ScheduledTask runTaskTimer( Runnable runnable, boolean async, long delay, long period, TimeUnit timeUnit )
            {
                return () ->
                {
                };
            }
        };
    }

    @Override
    public CommandManager<? extends SimpleCommand<?>, ? extends SimpleTabComplete<?>> getCommandManager()
    {
        return (CommandManager<SimpleCommand<?>, SimpleTabComplete<?>>) ( command, aliases, permission, simpleCommand, tabComplete, override ) ->
        {

        };
    }

    @Override
    public File getDataFolder()
    {
        return dataFolder;
    }

    @Override
    public void registerListeners( Object listenersInstance )
    {
    }
}
