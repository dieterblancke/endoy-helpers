package dev.endoy.helpers.database.migration;

public class MigrationException extends RuntimeException
{

    public MigrationException( String message )
    {
        super( message );
    }

    public MigrationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public MigrationException( Throwable cause )
    {
        super( cause );
    }
}