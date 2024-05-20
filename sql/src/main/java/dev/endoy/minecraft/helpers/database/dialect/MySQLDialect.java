package dev.endoy.minecraft.helpers.database.dialect;

import dev.endoy.minecraft.helpers.database.definitions.ColumnDataType;
import dev.endoy.minecraft.helpers.database.SQLDialect;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;

public class MySQLDialect extends SQLDialect
{

    public MySQLDialect()
    {
        defaultValueMappings.put( SqlValue.currentTimestamp(), "CURRENT_TIMESTAMP" );

        literalValueMappers.put( Boolean.class, value -> (boolean) value ? "1" : "0" );
        literalValueMappers.put( String.class, value -> "'" + value + "'" );

        columnDataTypeMappings.put( ColumnDataType.VARCHAR, "VARCHAR" );
        columnDataTypeMappings.put( ColumnDataType.TEXT, "TEXT" );
        columnDataTypeMappings.put( ColumnDataType.BOOLEAN, "BOOLEAN" );
        columnDataTypeMappings.put( ColumnDataType.TIMESTAMP, "TIMESTAMP" );
        columnDataTypeMappings.put( ColumnDataType.DATETIME, "DATETIME" );
        columnDataTypeMappings.put( ColumnDataType.FLOAT, "FLOAT" );
        columnDataTypeMappings.put( ColumnDataType.DOUBLE, "DOUBLE" );
        columnDataTypeMappings.put( ColumnDataType.DECIMAL, "DECIMAL" );
        columnDataTypeMappings.put( ColumnDataType.DATE, "DATE" );
        columnDataTypeMappings.put( ColumnDataType.TIME, "TIME" );
        columnDataTypeMappings.put( ColumnDataType.TINYINT, "TINYINT" );
        columnDataTypeMappings.put( ColumnDataType.SMALLINT, "SMALLINT" );
        columnDataTypeMappings.put( ColumnDataType.INT, "INT" );
        columnDataTypeMappings.put( ColumnDataType.BIGINT, "BIGINT" );
        columnDataTypeMappings.put( ColumnDataType.BIT, "BIT" );
        columnDataTypeMappings.put( ColumnDataType.SERIAL, "SERIAL" );
    }
}
