package dev.endoy.minecraft.helpers.database;

import dev.endoy.minecraft.helpers.database.definitions.ColumnDataType;
import dev.endoy.minecraft.helpers.database.definitions.ColumnDefinition;
import dev.endoy.minecraft.helpers.database.definitions.ConstraintDefinition;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue.LiteralSqlValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class SQLDialect
{

    protected Map<SqlValue, String> defaultValueMappings = new HashMap<>();
    protected Map<Class<?>, Function<Object, String>> literalValueMappers = new HashMap<>();
    protected Map<ColumnDataType, String> columnDataTypeMappings = new HashMap<>();

    public String createTable( String tableName, boolean ifNotExists, List<ColumnDefinition> columns )
    {
        StringBuilder createTableQueryBuilder = new StringBuilder();

        createTableQueryBuilder.append( "CREATE TABLE " );
        if ( ifNotExists )
        {
            createTableQueryBuilder.append( "IF NOT EXISTS " );
        }
        createTableQueryBuilder.append( tableName )
            .append( " (" );

        for ( int i = 0; i < columns.size(); i++ )
        {
            ColumnDefinition column = columns.get( i );

            if ( i > 0 )
            {
                createTableQueryBuilder.append( ", " );
            }

            createTableQueryBuilder.append( column.getName() )
                .append( " " )
                .append( columnDataTypeMappings.get( column.getDataType() ) );

            if ( column.getMaxLength() > 0 && column.getDataType().isSupportsSize() )
            {
                createTableQueryBuilder.append( "(" )
                    .append( column.getMaxLength() );

                if ( column.getDataType().isSupportsDecimalSize() )
                {
                    createTableQueryBuilder.append( "," )
                        .append( column.getDecimalSize() );
                }
                createTableQueryBuilder.append( ")" );
            }

            column.getConstraints().stream()
                .filter( ConstraintDefinition::isInline )
                .forEach( it -> createTableQueryBuilder.append( " " )
                    .append( it.buildConstraint( column.getName() ) ) );

            if ( column.getDefaultSqlValue() != null )
            {
                String defaultValue = mapDefaultValue( column.getDefaultSqlValue() );

                if ( defaultValue != null )
                {
                    createTableQueryBuilder.append( " DEFAULT " )
                        .append( defaultValue );
                }
            }
        }

        Map<ColumnDefinition, List<ConstraintDefinition>> tableConstraints = columns.stream()
            .filter( column -> column.getConstraints() != null && !column.getConstraints().isEmpty() )
            .collect( Collectors.toMap( it -> it, ColumnDefinition::getConstraints ) );

        if ( !tableConstraints.isEmpty() )
        {
            tableConstraints.forEach( ( column, constraints ) ->
            {
                for ( ConstraintDefinition constraint : constraints )
                {
                    if ( !constraint.isInline() )
                    {
                        createTableQueryBuilder.append( ", " )
                            .append( constraint.buildConstraint( column.getName() ) );
                    }
                }
            } );
        }

        createTableQueryBuilder.append( ");" );
        return createTableQueryBuilder.toString();
    }

    public String mapDefaultValue( SqlValue defaultSqlValue )
    {
        if ( defaultSqlValue instanceof LiteralSqlValue literalDefaultValue )
        {
            return literalValueMappers.getOrDefault( literalDefaultValue.getDefaultValue().getClass(), String::valueOf )
                .apply( literalDefaultValue.getDefaultValue() );
        }
        else
        {
            return defaultValueMappings.get( defaultSqlValue );
        }
    }
}
