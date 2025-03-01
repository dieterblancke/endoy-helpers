package dev.endoy.helpers.common.injector.exceptions;

public class CircularDependencyException extends RuntimeException
{

    public CircularDependencyException( String message )
    {
        super( message );
    }

    public CircularDependencyException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public CircularDependencyException( Throwable cause )
    {
        super( cause );
    }
}
