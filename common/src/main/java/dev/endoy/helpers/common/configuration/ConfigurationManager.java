package dev.endoy.helpers.common.configuration;

import dev.endoy.configuration.api.FileStorageType;
import dev.endoy.configuration.api.IConfiguration;
import dev.endoy.configuration.api.ISection;
import dev.endoy.configuration.json.JsonConfigurationOptions;
import dev.endoy.configuration.yaml.YamlConfiguration;
import dev.endoy.configuration.yaml.YamlConfigurationOptions;
import dev.endoy.configuration.yaml.comments.CommentType;
import dev.endoy.helpers.common.EndoyApplication;
import dev.endoy.helpers.common.injector.annotations.Comment;
import dev.endoy.helpers.common.injector.annotations.Configuration;
import dev.endoy.helpers.common.injector.annotations.ConfigurationSection;
import dev.endoy.helpers.common.injector.annotations.Value;
import dev.endoy.helpers.common.transform.TransformValue;
import dev.endoy.helpers.common.transform.ValueTransformer;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import dev.endoy.helpers.common.utils.Utils;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ConfigurationManager
{

    private final EndoyApplication endoyApplication;
    private final Map<String, IConfiguration> configurations = new ConcurrentHashMap<>();

    public void createDefault( Class<?> configClass )
    {
        this.validateConfig( configClass );

        Configuration configuration = configClass.getAnnotation( Configuration.class );
        File file = new File( endoyApplication.getDataFolder(), configuration.filePath() );

        if ( !file.exists() )
        {
            try
            {
                if ( !file.getParentFile().exists() )
                {
                    file.getParentFile().mkdirs();
                }

                file.createNewFile();
                this.save( endoyApplication.getInjector().getInjectableInstance( configClass ) );
            }
            catch ( IOException e )
            {
                throw new ConfigurationException( "Failed to create default configuration file", e );
            }
        }
        else
        {
            this.save( endoyApplication.getInjector().getInjectableInstance( configClass ) );
        }
    }

    public void save( Object config )
    {
        this.validateConfig( config.getClass() );

        Configuration configurationAnnotation = config.getClass().getAnnotation( Configuration.class );
        IConfiguration configuration = this.getOrLoadConfig( configurationAnnotation.fileType(), configurationAnnotation.filePath() );

        this.writeFieldsToConfiguration( configuration, config.getClass(), config, "" );

        try
        {
            configuration.save();
        }
        catch ( IOException e )
        {
            throw new ConfigurationException( "Failed to save configuration file", e );
        }
    }

    public IConfiguration reload( FileStorageType fileStorageType, String filePath )
    {
        IConfiguration configuration = this.getOrLoadConfig( fileStorageType, filePath );

        try
        {
            configuration.reload();
        }
        catch ( IOException e )
        {
            throw new ConfigurationException( "Failed to reload configuration file", e );
        }

        return configuration;
    }

    public IConfiguration getOrLoadConfig( FileStorageType fileStorageType, String filePath )
    {
        return configurations.computeIfAbsent( filePath, key -> IConfiguration.loadConfiguration(
            fileStorageType,
            new File( endoyApplication.getDataFolder(), filePath ),
            fileStorageType == FileStorageType.YAML
                ? YamlConfigurationOptions.builder().useComments( true ).build()
                : JsonConfigurationOptions.builder().build()
        ) );
    }

    private void writeFieldsToConfiguration( IConfiguration configuration, Class<?> clazz, Object instance, String prefix )
    {
        this.writeFieldsToConfiguration( configuration, configuration, clazz, instance, prefix );
    }

    private void writeFieldsToConfiguration( IConfiguration configuration, ISection currentSection, Class<?> clazz, Object instance, String prefix )
    {
        for ( Field declaredField : clazz.getDeclaredFields() )
        {
            if ( !declaredField.isAnnotationPresent( Value.class ) )
            {
                continue;
            }

            Value value = declaredField.getAnnotation( Value.class );
            String path = value.path().isEmpty() ? Utils.convertCamelCaseToDashNotation( declaredField.getName() ) : value.path();

            Object fieldValue;
            try
            {
                fieldValue = ReflectionUtils.getFieldValue( declaredField, instance );

                if ( declaredField.getType().isEnum() )
                {
                    fieldValue = fieldValue.toString();
                }
                else if ( declaredField.isAnnotationPresent( TransformValue.class ) )
                {
                    TransformValue transformValue = declaredField.getAnnotation( TransformValue.class );
                    ValueTransformer transformer = ValueTransformerRegistry.getOrCreateValueTransformer( transformValue.value() );

                    fieldValue = transformer.transformToConfigValue( fieldValue );
                }
            }
            catch ( IllegalAccessException e )
            {
                continue;
            }

            if ( configuration instanceof YamlConfiguration yamlConfiguration && declaredField.isAnnotationPresent( Comment.class ) )
            {
                Comment comment = declaredField.getAnnotation( Comment.class );
                yamlConfiguration.setComment( prefix + path, String.join( "\n", comment.value() ), CommentType.BLOCK );
            }

            if ( declaredField.getType().isAnnotationPresent( ConfigurationSection.class ) )
            {
                ISection section = currentSection.exists( path ) ? currentSection.getSection( path ) : currentSection.createSection( path );
                writeFieldsToConfiguration( configuration, section, declaredField.getType(), fieldValue, prefix + path + "." );
                currentSection.set( path, section );
            }
            else
            {
                if ( !currentSection.exists( path ) )
                {
                    currentSection.set( path, fieldValue );
                }
            }
        }
    }

    private void validateConfig( Class<?> configClass )
    {
        if ( !configClass.isAnnotationPresent( Configuration.class ) )
        {
            throw new ConfigurationException( "Class " + configClass.getName() + " is not annotated with @Configuration" );
        }
    }
}
