package dev.endoy.helpers.common.injector;

import dev.endoy.configuration.api.FileStorageType;
import dev.endoy.configuration.api.IConfiguration;
import dev.endoy.configuration.api.ISection;
import dev.endoy.helpers.common.EndoyApplication;
import dev.endoy.helpers.common.configuration.ValueTransformerRegistry;
import dev.endoy.helpers.common.transform.TransformValue;
import dev.endoy.helpers.common.transform.ValueTransformer;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import dev.endoy.helpers.common.utils.Utils;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class ConfigurationInjector
{

    private final Injector injector;
    private final EndoyApplication endoyApplication;

    public static ConfigurationInjector forInjector( Injector injector, EndoyApplication endoyApplication )
    {
        return new ConfigurationInjector( injector, endoyApplication );
    }

    public void reloadConfigurations()
    {
        this.injector.getInjectablesOfType( Configuration.class )
            .forEach( instance ->
            {
                Configuration configurationAnnotation = instance.getClass().getAnnotation( Configuration.class );
                IConfiguration configuration = endoyApplication.getConfigurationManager().reload(
                    configurationAnnotation.fileType(),
                    configurationAnnotation.filePath()
                );

                this.injectConfigurationFields(
                    instance.getClass(),
                    instance,
                    configuration,
                    ""
                );
            } );
    }

    void inject()
    {
        this.injector.initializeInjectablesOfType(
            Configuration.class,
            configurations -> configurations.forEach( configuration ->
                {
                    endoyApplication.getConfigurationManager().createDefault( configuration.instance().getClass() );

                    this.injectConfigurationFields(
                        configuration.instance().getClass(),
                        configuration.instance(),
                        configuration.annotation()
                    );
                }
            )
        );
    }

    void injectConfigurationFields( Class<?> clazz, Object instance, Configuration configuration )
    {
        this.injectConfigurationFields(
            clazz,
            instance,
            endoyApplication.getConfigurationManager().getOrLoadConfig( configuration.fileType(), configuration.filePath() ),
            ""
        );
    }

    void injectConfigurationFields( Class<?> clazz, Object instance )
    {
        if ( clazz.isAnnotationPresent( Configuration.class ) )
        {
            return;
        }
        this.injectConfigurationFields(
            clazz,
            instance,
            endoyApplication.getConfigurationManager().getOrLoadConfig( FileStorageType.YAML, "config.yml" ),
            ""
        );
    }

    private void injectConfigurationFields( Class<?> clazz, Object instance, IConfiguration configuration, String prefix )
    {
        Arrays.stream( clazz.getDeclaredFields() )
            .filter( field -> field.isAnnotationPresent( Value.class ) )
            .forEach( field ->
            {
                try
                {
                    Value value = field.getAnnotation( Value.class );

                    if ( field.getType().isAnnotationPresent( ConfigurationSection.class ) )
                    {
                        Class<?> fieldClass = field.getType();
                        Object fieldInstance = fieldClass.getDeclaredConstructors()[0].newInstance();

                        injectConfigurationFields(
                            field.getType(),
                            fieldInstance,
                            configuration,
                            prefix + ( value.path().isEmpty() ? Utils.convertCamelCaseToDashNotation( field.getName() ) : value.path() ) + "."
                        );

                        ReflectionUtils.setFieldValue( field, instance, fieldInstance );
                    }
                    else if ( field.isAnnotationPresent( Value.class ) )
                    {
                        Object configValue = getConfigurationValue( configuration, prefix, field.getName(), value );

                        if ( configValue == null ) // allow default values to flourish
                        {
                            return;
                        }

                        this.setConfigurationValue( field, instance, configValue );
                    }
                }
                catch ( InstantiationException | InvocationTargetException | IllegalAccessException |
                        NoSuchMethodException e )
                {
                    throw new FailedInjectionException( e );
                }
            } );
    }

    public Object getConfigurationValue( Parameter parameter, Value value )
    {
        return getConfigurationValue(
            endoyApplication.getConfigurationManager().getOrLoadConfig( FileStorageType.YAML, "config.yml" ),
            "",
            parameter.getName(),
            value
        );
    }

    public Object getConfigurationValue( IConfiguration configuration, String prefix, String name, Value value )
    {
        return configuration.get( prefix + ( value.path().isEmpty() ? Utils.convertCamelCaseToDashNotation( name ) : value.path() ) );
    }

    private void setConfigurationValue( Field field, Object instance, Object configValue ) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException
    {
        if ( field.getType().isEnum() )
        {
            configValue = Enum.valueOf( (Class<Enum>) field.getType(), String.valueOf( configValue ) );
        }
        else if ( Map.class.isAssignableFrom( field.getType() ) && configValue instanceof ISection section )
        {
            Map<String, Object> mapInstance = field.getType().isInterface() ? new HashMap<>() : (Map<String, Object>) field.getType().getDeclaredConstructor().newInstance();

            for ( String key : section.getKeys() )
            {
                mapInstance.put( key, section.get( key ) );
            }

            configValue = mapInstance;
        }
        else if ( field.isAnnotationPresent( TransformValue.class ) )
        {
            TransformValue transformValue = field.getAnnotation( TransformValue.class );
            ValueTransformer<?> transformer = ValueTransformerRegistry.getOrCreateValueTransformer( transformValue.value() );

            configValue = transformer.transformFromConfigValue( configValue );
        }

        ReflectionUtils.setFieldValue( field, instance, configValue );
    }
}
