package dev.endoy.helpers.common.injector;

import dev.endoy.configuration.api.IConfiguration;
import dev.endoy.helpers.common.EndoyApplication;
import dev.endoy.helpers.common.command.CommandManager;
import dev.endoy.helpers.common.command.SimpleTabComplete;
import dev.endoy.helpers.common.task.TaskExecutionException;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Injector
{

    private final Class<?> currentClass;
    private final Map<Class<?>, Object> injectables = new ConcurrentHashMap<>();
    @Getter
    private final ConfigurationInjector configurationInjector;
    private final EndoyApplication endoyApplication;

    public Injector( Class<?> clazz, EndoyApplication endoyApplication )
    {
        this.currentClass = clazz;
        this.configurationInjector = ConfigurationInjector.forInjector( this, endoyApplication );
        this.endoyApplication = endoyApplication;
    }

    public static Injector forProject( Class<?> clazz, EndoyApplication endoyApplication )
    {
        return new Injector( clazz, endoyApplication );
    }

    public void registerInjectable( Class<?> clazz, Object instance )
    {
        if ( instance == null )
        {
            throw new IllegalArgumentException( "Instance cannot be null" );
        }

        this.injectables.put( clazz, instance );

        while ( clazz.getSuperclass() != null && !clazz.getSuperclass().equals( Object.class ) )
        {
            clazz = clazz.getSuperclass();

            if ( !this.injectables.containsKey( clazz ) )
            {
                this.injectables.put( clazz, instance );
            }
        }

        for ( Class<?> interfaze : clazz.getInterfaces() )
        {
            if ( !this.injectables.containsKey( interfaze ) )
            {
                this.injectables.put( interfaze, instance );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    public <T> T getInjectableInstance( Class<T> injectableClass )
    {
        return (T) this.injectables.get( injectableClass );
    }

    public void inject()
    {
        this.validateInjectableConstructors();

        this.configurationInjector.inject();
        this.initializeBeans();
        this.initializeCommands();
        this.initializeListeners();
        this.initializeComponents();
        this.initializeManagers();
        this.initializeServices();

        this.injectables.forEach( this::injectFields );
        this.injectables.forEach( configurationInjector::injectConfigurationFields );
        this.injectables.forEach( this::executePostConstructs );
        this.injectables.forEach( this::initializeTasks );
    }

    private void validateInjectableConstructors()
    {
        this.getInjectableAnnotations()
            .stream()
            .flatMap( annotation -> ReflectionUtils.getClassesInPackageAnnotatedWith( this.currentClass, annotation ).stream() )
            .forEach( clazz ->
            {
                if ( clazz.getDeclaredConstructors().length != 1 )
                {
                    throw new InvalidInjectionContextException( "Injectable class must have exactly one constructor: " + clazz.getName() );
                }
            } );
    }

    private void initializeBeans()
    {
        this.initializeInjectablesOfType( Beans.class, beans -> beans.forEach( bean ->
        {
            Arrays.stream( bean.instance().getClass().getDeclaredMethods() )
                .filter( it -> it.isAnnotationPresent( Bean.class ) )
                .filter( it -> it.getParameters().length == 0 || Arrays.stream( it.getParameters() ).allMatch( param -> this.isInjectable( param.getType() ) ) )
                .forEach( method ->
                {
                    try
                    {
                        method.setAccessible( true );
                        Object value = method.invoke(
                            bean.instance(),
                            Arrays.stream( method.getParameters() )
                                .map( parameter -> this.findOrRegisterInjectable( parameter.getType() ) )
                                .toArray()
                        );

                        if ( value != null )
                        {
                            this.registerInjectable( value.getClass(), value );
                        }
                        method.setAccessible( false );
                    }
                    catch ( Exception e )
                    {
                        throw new BeanException( "Failed to create bean: " + method.getName() + " in class " + bean.instance().getClass().getName(), e );
                    }
                } );
        } ) );
    }

    @SuppressWarnings( "unchecked" )
    private void initializeCommands()
    {
        this.initializeInjectablesOfType( Command.class, commands -> commands.forEach( command ->
        {
            Command commandAnnotation = command.annotation();
            CommandManager commandManager = endoyApplication.getCommandManager();

            commandManager.registerCommand(
                commandAnnotation.command(),
                List.of( commandAnnotation.aliases() ),
                commandAnnotation.permission(),
                command.instance(),
                command.instance() instanceof SimpleTabComplete ? command.instance() : null,
                commandAnnotation.override()
            );
        } ) );
    }

    private void initializeListeners()
    {
        this.initializeInjectablesOfType( Listeners.class, listeners ->
            listeners.forEach( listener -> this.endoyApplication.registerListeners( listener.instance ) ) );
    }

    private void initializeComponents()
    {
        this.initializeInjectablesOfType( Component.class );
    }

    private void initializeManagers()
    {
        this.initializeInjectablesOfType( Manager.class );
    }

    private void initializeServices()
    {
        this.initializeInjectablesOfType( Service.class );
    }

    private void initializeTasks( Class<?> clazz, Object instance )
    {
        Arrays.stream( clazz.getDeclaredMethods() )
            .filter( method -> method.isAnnotationPresent( Task.class ) )
            .filter( it -> it.getParameters().length == 0 )
            .forEach( method ->
            {
                if ( !Modifier.isPublic( method.getModifiers() ) )
                {
                    throw new TaskExecutionException( "Task method must be public: " + method.getName() + " in class " + clazz.getName() );
                }

                Task task = method.getAnnotation( Task.class );

                endoyApplication.getTaskManager().registerTask( task, () ->
                {
                    try
                    {
                        method.invoke( instance );
                    }
                    catch ( Exception e )
                    {
                        throw new TaskExecutionException( "Failed to execute task: " + method.getName() + " in class " + clazz.getName(), e );
                    }
                } );
            } );
    }

    <T extends Annotation> void initializeInjectablesOfType( Class<T> annotationClass )
    {
        this.initializeInjectablesOfType( annotationClass, null );
    }

    <T extends Annotation> void initializeInjectablesOfType( Class<T> annotationClass, Consumer<List<InjectedType<T>>> injectedTypesConsumer )
    {
        List<InjectedType<T>> injectedTypes = ReflectionUtils.getClassesInPackageAnnotatedWith( this.currentClass, annotationClass )
            .stream()
            .filter( this::checkConditionals )
            .map( clazz ->
            {
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                this.validateConstructor( constructor );

                return new InjectedType<>( clazz.getAnnotation( annotationClass ), this.initializeInjectable( clazz ) );
            } )
            .collect( Collectors.toList() );

        if ( injectedTypesConsumer != null )
        {
            injectedTypesConsumer.accept( injectedTypes );
        }
    }

    private Object initializeInjectable( Class<?> clazz )
    {
        if ( this.isInterfaceOrAbstract( clazz ) )
        {
            return initializeInjectable( getInjectableClassFromParentClass( clazz ) );
        }

        try
        {
            Class<? extends Annotation> annotation = this.getInjectableAnnotation( clazz );
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible( true );

            List<Object> parameters = new ArrayList<>();

            for ( Parameter parameter : constructor.getParameters() )
            {
                if ( this.injectables.containsKey( parameter.getType() ) )
                {
                    parameters.add( this.injectables.get( parameter.getType() ) );
                }
                else
                {
                    if ( this.isInjectable( parameter.getType() ) )
                    {
                        parameters.add( this.initializeInjectable( parameter.getType() ) );
                    }
                    else if ( parameter.isAnnotationPresent( Value.class ) )
                    {
                        Value value = parameter.getAnnotation( Value.class );

                        parameters.add( this.configurationInjector.getConfigurationValue( parameter, value ) );
                    }
                    else
                    {
                        throw new InvalidInjectionContextException( "Parameter is not injectable: " + parameter.getName() + " in class " + clazz.getName() );
                    }
                }
            }

            Object instance = constructor.newInstance( parameters.toArray() );

            constructor.setAccessible( false );
            this.registerInjectable( clazz, instance );

            if ( annotation.equals( Configuration.class ) )
            {
                this.configurationInjector.injectConfigurationFields( clazz, instance, clazz.getAnnotation( Configuration.class ) );
            }

            return instance;
        }
        catch ( Exception e )
        {
            throw new FailedInjectionException( "Failed to initialize injectable: " + clazz.getName(), e );
        }
    }

    private void injectFields( Class<?> clazz, Object instance )
    {
        Arrays.stream( clazz.getDeclaredFields() )
            .filter( field -> field.isAnnotationPresent( Inject.class ) )
            .forEach( field ->
            {
                try
                {
                    ReflectionUtils.setFieldValue( field, instance, this.findOrRegisterInjectable( field.getType() ) );
                }
                catch ( Exception e )
                {
                    throw new FailedInjectionException( "Failed to inject field: " + field.getName() + " in class " + clazz.getName(), e );
                }
            } );
    }

    private void executePostConstructs( Class<?> clazz, Object instance )
    {
        Arrays.stream( clazz.getDeclaredMethods() )
            .filter( method -> method.isAnnotationPresent( PostConstruct.class ) )
            .forEach( method ->
            {
                if ( method.getParameters().length != 0 )
                {
                    throw new PostConstructException( "PostConstruct method must not have any parameters: " + method.getName() + " in class " + clazz.getName() );
                }

                try
                {
                    ReflectionUtils.invokeMethod( method, instance );
                }
                catch ( Exception e )
                {
                    throw new PostConstructException( "Failed to execute post construct method: " + method.getName() + " in class " + clazz.getName(), e );
                }
            } );
    }

    private void validateConstructor( Constructor<?> constructor )
    {
        List<Parameter> nonInjectableParameters = Arrays.stream( constructor.getParameters() )
            .filter( parameter -> !this.isInjectable( parameter.getType() ) && !parameter.isAnnotationPresent( Value.class ) )
            .toList();
        if ( !nonInjectableParameters.isEmpty() )
        {
            throw new InvalidInjectionContextException(
                String.format(
                    "All parameters of an Injectable constructor must be injectable: %s.%n" +
                        "The following parameters could not be injected: %s",
                    constructor.getDeclaringClass().getName(),
                    nonInjectableParameters.stream()
                        .map( parameter -> String.format( "%s (position %d)", parameter.getType().getName(), Arrays.asList( constructor.getParameters() ).indexOf( parameter ) ) )
                        .collect( Collectors.joining( ", " ) )
                )
            );
        }

        this.checkForCircularDependencies( constructor.getDeclaringClass(), new HashSet<>() );
    }

    private void checkForCircularDependencies( Class<?> clazz, Set<Class<?>> visitedDependencies )
    {
        if ( visitedDependencies.contains( clazz ) )
        {
            throw new CircularDependencyException( "Circular dependency detected: " + clazz.getName() );
        }

        visitedDependencies.add( clazz );

        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];

        for ( Parameter parameter : constructor.getParameters() )
        {
            if ( this.isInjectable( parameter.getType() ) )
            {
                this.checkForCircularDependencies( parameter.getType(), visitedDependencies );
            }
        }

        visitedDependencies.remove( clazz );
    }

    private boolean isInjectable( Class<?> clazz )
    {
        if ( this.isInterfaceOrAbstract( clazz ) )
        {
            Collection<Class<?>> classes = ReflectionUtils.getClassesInPackageImplementing( this.currentClass, clazz );

            return classes.stream().anyMatch( this::isInjectable );
        }

        return this.injectables.containsKey( clazz ) || this.getInjectableAnnotations()
            .stream()
            .anyMatch( clazz::isAnnotationPresent );
    }

    private Class<?> getInjectableClassFromParentClass( Class<?> clazz )
    {
        if ( !this.isInterfaceOrAbstract( clazz ) )
        {
            throw new IllegalStateException( "Class is not an interface or abstract class: " + clazz.getName() );
        }

        Collection<Class<?>> classes = ReflectionUtils.getClassesInPackageImplementing( this.currentClass, clazz );

        return classes
            .stream()
            .filter( this::checkConditionals )
            .findFirst()
            .orElseThrow( () -> new InvalidInjectionContextException( "Class is not an injectable: " + clazz.getName() ) );
    }

    private Class<? extends Annotation> getInjectableAnnotation( Class<?> clazz )
    {
        return this.getInjectableAnnotations()
            .stream()
            .filter( clazz::isAnnotationPresent )
            .findFirst()
            .orElseThrow( () -> new InvalidInjectionContextException( "Class is not an injectable: " + clazz.getName() ) );
    }

    private List<Class<? extends Annotation>> getInjectableAnnotations()
    {
        return List.of( Configuration.class, Beans.class, Command.class, Listeners.class, Component.class, Manager.class, Service.class, Task.class );
    }

    private boolean checkConditionals( Class<?> clazz )
    {
        if ( clazz.isAnnotationPresent( ConditionalOnConfigProperty.class ) )
        {
            ConditionalOnConfigProperty conditionalOnConfigProperty = clazz.getAnnotation( ConditionalOnConfigProperty.class );
            IConfiguration configuration = endoyApplication.getConfigurationManager().getOrLoadConfig(
                conditionalOnConfigProperty.fileType(),
                conditionalOnConfigProperty.filePath()
            );
            Object value = configuration.get( conditionalOnConfigProperty.propertyPath() );

            if ( value == null && conditionalOnConfigProperty.matchIfMissing() )
            {
                return true;
            }

            return Objects.equals( String.valueOf( value ), conditionalOnConfigProperty.havingValue() );
        }

        return true;
    }

    private boolean isInterfaceOrAbstract( Class<?> clazz )
    {
        return clazz.isInterface() || Modifier.isAbstract( clazz.getModifiers() );
    }

    public List<Object> getInjectablesOfType( Class<? extends Annotation> annotation )
    {
        return this.injectables.entrySet()
            .stream()
            .filter( entry -> entry.getKey().isAnnotationPresent( annotation ) )
            .map( Entry::getValue )
            .collect( Collectors.toList() );
    }

    private Object findOrRegisterInjectable( Class<?> clazz )
    {
        Class<?> mappedClass = clazz;

        if ( this.isInterfaceOrAbstract( mappedClass ) && this.isInjectable( mappedClass ) )
        {
            mappedClass = this.getInjectableClassFromParentClass( mappedClass );
        }

        Object value;

        if ( this.injectables.containsKey( mappedClass ) )
        {
            value = this.injectables.get( mappedClass );
        }
        else
        {
            value = this.initializeInjectable( clazz );
        }

        return value;
    }

    record InjectedType<T>(T annotation, Object instance)
    {
    }
}
