package dev.endoy.minecraft.helpers.database.definitions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Selection
{

    private final String selection;

    public static Selection all()
    {
        return new Selection( "*" );
    }

    public static Selection count()
    {
        return new Selection( "COUNT(*)" );
    }

    public static Selection count( String column )
    {
        return new Selection( "COUNT(" + column + ")" );
    }

    public static Selection sum( String column )
    {
        return new Selection( "SUM(" + column + ")" );
    }

    public static Selection avg( String column )
    {
        return new Selection( "AVG(" + column + ")" );
    }

    public static Selection max( String column )
    {
        return new Selection( "MAX(" + column + ")" );
    }

    public static Selection min( String column )
    {
        return new Selection( "MIN(" + column + ")" );
    }

    public static Selection distinct( String column )
    {
        return new Selection( "DISTINCT " + column );
    }

    public static Selection exists( String column )
    {
        return new Selection( "EXISTS(" + column + ")" );
    }

    public static Selection column( String column )
    {
        return new Selection( column );
    }
}
