package dev.endoy.helpers.common.injector.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Command
{

    String command();

    String[] aliases() default {};

    String permission();

    boolean override() default true;

}
