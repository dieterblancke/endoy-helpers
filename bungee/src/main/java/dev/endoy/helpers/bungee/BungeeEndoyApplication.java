package dev.endoy.helpers.bungee;

import dev.endoy.helpers.bungee.command.BungeeCommandManager;
import dev.endoy.helpers.bungee.task.BungeeTaskManager;
import dev.endoy.helpers.common.EndoyApplication;
import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.command.SimpleCommand;
import dev.endoy.helpers.common.command.SimpleTabComplete;
import dev.endoy.helpers.common.injector.Injector;
import dev.endoy.helpers.common.task.TaskManager;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.util.Arrays;

public class BungeeEndoyApplication extends EndoyApplication
{

    private final Class<?> currentClass;
    @Getter
    private final Injector injector;
    private final BungeeTaskManager bungeeTaskManager;
    private final BungeeCommandManager bungeeCommandManager;
    @Getter
    private final Plugin plugin;

    private BungeeEndoyApplication( Plugin plugin, Class<?> clazz )
    {
        super();

        this.plugin = plugin;
        this.currentClass = clazz;
        this.bungeeTaskManager = new BungeeTaskManager( plugin );
        this.bungeeCommandManager = new BungeeCommandManager( plugin );

        this.injector = Injector.forProject( this.currentClass, this );
        this.registerDefaultInjectables();
    }

    public static BungeeEndoyApplication forPlugin( Plugin plugin )
    {
        return new BungeeEndoyApplication( plugin, plugin.getClass() );
    }

    @Override
    public TaskManager getTaskManager()
    {
        return this.bungeeTaskManager;
    }

    @Override
    public CommandManager<? extends SimpleCommand<?>, ? extends SimpleTabComplete<?>> getCommandManager()
    {
        return bungeeCommandManager;
    }

    @Override
    public File getDataFolder()
    {
        return this.plugin.getDataFolder();
    }

    @Override
    public void registerListeners( Object listenersInstance )
    {
        if ( !Listener.class.isAssignableFrom( listenersInstance.getClass() ) )
        {
            // TODO: replace with logger
            System.out.println( "Class " + listenersInstance.getClass().getName() + " was skipped as it does not Implement Listener Class" );
            return;
        }
        if ( Arrays.stream( listenersInstance.getClass().getMethods() ).noneMatch( method -> method.isAnnotationPresent( EventHandler.class ) ) )
        {
            // TODO: replace with logger
            System.out.println( "Class " + listenersInstance.getClass().getName() + " was skipped as it does not have any methods with @EventHandler annotation." );
            return;
        }
        ProxyServer.getInstance().getPluginManager().registerListener( this.plugin, (Listener) listenersInstance );
    }

    @Override
    public void registerDefaultInjectables()
    {
        super.registerDefaultInjectables();

        this.injector.registerInjectable( BungeeEndoyApplication.class, this );
        this.injector.registerInjectable( Injector.class, this.injector );
        this.injector.registerInjectable( plugin.getClass(), plugin );
    }

    public void inject()
    {
        this.injector.inject();
    }
}
