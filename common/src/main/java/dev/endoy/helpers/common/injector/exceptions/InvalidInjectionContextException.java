package dev.endoy.helpers.common.injector.exceptions;

public class InvalidInjectionContextException extends RuntimeException
{

    public InvalidInjectionContextException( String message )
    {
        super( message );
    }

    public InvalidInjectionContextException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public InvalidInjectionContextException( Throwable cause )
    {
        super( cause );
    }
}
