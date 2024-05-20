package dev.endoy.minecraft.helpers.database.query.table;

import dev.endoy.minecraft.helpers.database.definitions.ColumnDataType;
import dev.endoy.minecraft.helpers.database.definitions.ColumnDefinition;
import dev.endoy.minecraft.helpers.database.definitions.ConstraintDefinition;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ColumnQueryBuilder
{

    private final TableQueryBuilder tableQueryBuilder;
    private String columnName;
    private ColumnDataType columnType;
    private int maxLength;
    private int decimalSize;
    private List<ConstraintDefinition> constraints = new ArrayList<>();
    private SqlValue defaultSqlValue;

    public ColumnQueryBuilder name( String columnName )
    {
        this.columnName = columnName;
        return this;
    }

    public ColumnQueryBuilder type( ColumnDataType columnType )
    {
        this.columnType = columnType;
        return this;
    }

    public ColumnQueryBuilder maxLength( int maxLength )
    {
        this.maxLength = maxLength;
        return this;
    }

    public ColumnQueryBuilder decimalSize( int decimalSize )
    {
        this.decimalSize = decimalSize;
        return this;
    }

    public ConstraintBuilder addConstraint()
    {
        return new ConstraintBuilder( this );
    }

    public ColumnQueryBuilder addConstraint( ConstraintDefinition constraint )
    {
        constraints.add( constraint );
        return this;
    }

    public ColumnQueryBuilder defaultValue( SqlValue defaultSqlValue )
    {
        this.defaultSqlValue = defaultSqlValue;
        return this;
    }

    public TableQueryBuilder and()
    {
        return tableQueryBuilder.addColumn( new ColumnDefinition(
            columnName,
            columnType,
            maxLength,
            decimalSize,
            constraints,
            defaultSqlValue
        ) );
    }
}
