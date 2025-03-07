package dev.endoy.helpers.common.injector;

public class BeanException extends RuntimeException
{

    public BeanException( String message )
    {
        super( message );
    }

    public BeanException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public BeanException( Throwable cause )
    {
        super( cause );
    }
}
