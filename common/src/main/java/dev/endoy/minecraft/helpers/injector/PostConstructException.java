package dev.endoy.minecraft.helpers.injector;

public class PostConstructException extends RuntimeException
{

    public PostConstructException( String message )
    {
        super( message );
    }

    public PostConstructException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public PostConstructException( Throwable cause )
    {
        super( cause );
    }
}
