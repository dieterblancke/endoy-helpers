package dev.endoy.minecraft.helpers.database.definitions;


import lombok.Value;

public interface SqlValue
{

    static LiteralSqlValue literal( Object defaultValue )
    {
        return new LiteralSqlValue( defaultValue );
    }

    static CurrentTimestampSqlValue currentTimestamp()
    {
        return new CurrentTimestampSqlValue();
    }

    @Value
    class LiteralSqlValue implements SqlValue
    {
        Object defaultValue;
    }

    @Value
    class CurrentTimestampSqlValue implements SqlValue
    {
    }
}
