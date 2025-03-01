package dev.endoy.helpers.common.task;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import dev.endoy.helpers.common.injector.annotations.Task;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

public interface TaskManager
{

    default void registerTask( Task task, Runnable runnable )
    {
        if ( task.cron().isEmpty() )
        {
            if ( task.initialDelay() > 0 )
            {
                if ( task.fixedDelay() > 0 )
                {
                    runTaskTimer( runnable, task.async(), task.initialDelay(), task.fixedDelay(), task.timeUnit() );
                }
                else
                {
                    runTaskLater( runnable, task.async(), task.initialDelay(), task.timeUnit() );
                }
            }
            else
            {
                if ( task.fixedDelay() > 0 )
                {
                    runTaskTimer( runnable, task.async(), task.fixedDelay(), task.fixedDelay(), task.timeUnit() );
                }
                else
                {
                    runTask( runnable, task.async() );
                }
            }
        }
        else
        {
            Cron cron = new CronParser( CronDefinitionBuilder.instanceDefinitionFor( CronType.SPRING ) ).parse( task.cron() );

            runTaskCron( runnable, cron, task.async() );
        }
    }

    default void runTaskCron( Runnable runnable, Cron cron, boolean async )
    {
        ExecutionTime executionTime = ExecutionTime.forCron( cron );

        executionTime.timeToNextExecution( ZonedDateTime.now() )
            .ifPresent( timeToNextExecution ->
            {
                long delay = timeToNextExecution.toMillis();

                runTaskLater( () ->
                {
                    runnable.run();
                    runTaskCron( runnable, cron, async );
                }, async, delay, TimeUnit.MILLISECONDS );
            } );
    }

    ScheduledTask runTask( Runnable runnable, boolean async );

    ScheduledTask runTaskLater( Runnable runnable, boolean async, long delay, TimeUnit timeUnit );

    ScheduledTask runTaskTimer( Runnable runnable, boolean async, long delay, long period, TimeUnit timeUnit );

}
