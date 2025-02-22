package dev.endoy.helpers.spigot;

import dev.endoy.helpers.common.EndoyApplication;
import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.command.SimpleCommand;
import dev.endoy.helpers.common.command.SimpleTabComplete;
import dev.endoy.helpers.common.injector.Injector;
import dev.endoy.helpers.common.task.TaskManager;
import dev.endoy.helpers.spigot.command.SpigotCommandManager;
import dev.endoy.helpers.spigot.task.SpigotTaskManager;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Arrays;

public class SpigotEndoyApplication extends EndoyApplication
{

    private final Class<?> currentClass;
    @Getter
    private final Injector injector;
    private final SpigotTaskManager spigotTaskManager;
    private final SpigotCommandManager spigotCommandManager;
    @Getter
    private final Plugin plugin;

    private SpigotEndoyApplication( Plugin plugin, Class<?> clazz )
    {
        super();

        this.plugin = plugin;
        this.currentClass = clazz;
        this.spigotTaskManager = new SpigotTaskManager( plugin );
        this.spigotCommandManager = new SpigotCommandManager();

        this.injector = Injector.forProject( this.currentClass, this );
        this.registerDefaultInjectables();
    }

    public static SpigotEndoyApplication forPlugin( Plugin plugin )
    {
        return new SpigotEndoyApplication( plugin, plugin.getClass() );
    }

    @Override
    public TaskManager getTaskManager()
    {
        return this.spigotTaskManager;
    }

    @Override
    public CommandManager<? extends SimpleCommand<?>, ? extends SimpleTabComplete<?>> getCommandManager()
    {
        return spigotCommandManager;
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
        this.plugin.getServer().getPluginManager().registerEvents( (Listener) listenersInstance, this.plugin );
    }

    @Override
    public void registerDefaultInjectables()
    {
        super.registerDefaultInjectables();

        this.injector.registerInjectable( SpigotEndoyApplication.class, this );
        this.injector.registerInjectable( Injector.class, this.injector );
        this.injector.registerInjectable( plugin.getClass(), plugin );
    }

    public void inject()
    {
        this.injector.inject();
    }
}
