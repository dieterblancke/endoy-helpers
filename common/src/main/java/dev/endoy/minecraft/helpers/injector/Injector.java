package dev.endoy.minecraft.helpers.injector;

import dev.endoy.minecraft.helpers.EndoyApplication;
import dev.endoy.minecraft.helpers.task.TaskExecutionException;
import dev.endoy.minecraft.helpers.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Injector
{

    private final Class<?> currentClass;
    private final Map<Class<?>, Object> injectables = new HashMap<>();
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
        this.injectConfigurations();
        this.initializeCommands();
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

    private void injectConfigurations()
    {
        this.initializeInjectablesOfType( Configuration.class );
    }

    private void initializeCommands()
    {
        // TODO: perhaps change this to a type similar to Tasks?
//        this.initializeInjectablesOfType( Command.class, commands ->
//        {
//            // TODO: implement
//        } );
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
                        Object injectable = this.initializeInjectable( parameter.getType() );
                        this.injectables.put( parameter.getType(), injectable );
                        parameters.add( injectable );
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
            this.injectables.put( clazz, instance );

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
                    Object value;

                    if ( this.injectables.containsKey( field.getType() ) )
                    {
                        value = this.injectables.get( field.getType() );
                    }
                    else
                    {
                        Object injectable = this.initializeInjectable( field.getType() );
                        this.injectables.put( field.getType(), injectable );
                        value = injectable;
                    }

                    ReflectionUtils.setFieldValue( field, instance, value );
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
        if ( !Arrays.stream( constructor.getParameters() ).allMatch( parameter -> this.isInjectable( parameter.getType() ) || parameter.isAnnotationPresent( Value.class ) ) )
        {
            throw new InvalidInjectionContextException( "All parameters of an Injectable constructor must be injectable: " + constructor.getDeclaringClass().getName() );
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
    }

    private boolean isInjectable( Class<?> clazz )
    {
        return this.getInjectableAnnotations()
            .stream()
            .anyMatch( clazz::isAnnotationPresent );
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
        return List.of( Configuration.class, Command.class, Component.class, Manager.class, Service.class, Task.class );
    }

    record InjectedType<T>(T annotation, Object instance)
    {
    }
}
