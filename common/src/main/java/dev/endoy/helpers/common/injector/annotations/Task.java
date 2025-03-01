package dev.endoy.helpers.common.injector.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface Task
{

    String cron() default "";

    long fixedDelay() default -1;

    long initialDelay() default -1;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    boolean async() default false;

}
