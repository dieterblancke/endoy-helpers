package dev.endoy.helpers.common;

import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.command.SimpleCommand;
import dev.endoy.helpers.common.command.SimpleTabComplete;
import dev.endoy.helpers.common.configuration.ConfigurationManager;
import dev.endoy.helpers.common.injector.Injector;
import dev.endoy.helpers.common.task.TaskManager;
import lombok.Getter;

import java.io.File;

public abstract class EndoyApplication
{

    @Getter
    private final ConfigurationManager configurationManager;

    public EndoyApplication()
    {
        this.configurationManager = new ConfigurationManager( this );
    }

    public abstract TaskManager getTaskManager();

    public abstract CommandManager<? extends SimpleCommand<?>, ? extends SimpleTabComplete<?>> getCommandManager();

    public abstract File getDataFolder();

    public abstract Injector getInjector();

    public abstract void registerListeners( Object listenersInstance );

    public void registerDefaultInjectables()
    {
        this.getInjector().registerInjectable( ConfigurationManager.class, this.configurationManager );
        this.getInjector().registerInjectable( TaskManager.class, this.getTaskManager() );
        this.getInjector().registerInjectable( EndoyApplication.class, this );
    }

    /**
     * This will reload all configurations that are annotated with {@link dev.endoy.helpers.common.injector.Configuration}.
     *
     * It will, of course, not reload things that make use of the configuration values nor does it reload @Value fields.
     */
    public void reload()
    {
        this.getInjector().getConfigurationInjector().reloadConfigurations();
    }
}
