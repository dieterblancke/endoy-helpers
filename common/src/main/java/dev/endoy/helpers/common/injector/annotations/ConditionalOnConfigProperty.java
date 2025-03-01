package dev.endoy.helpers.common.injector.annotations;

import dev.endoy.configuration.api.FileStorageType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ConditionalOnConfigProperty
{

    FileStorageType fileType() default FileStorageType.YAML;

    String filePath() default "config.yml";

    String propertyPath();

    String havingValue();

    boolean matchIfMissing() default false;

}
