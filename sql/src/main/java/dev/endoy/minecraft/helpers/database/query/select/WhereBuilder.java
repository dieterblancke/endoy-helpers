package dev.endoy.minecraft.helpers.database.query.select;

import dev.endoy.minecraft.helpers.database.definitions.SqlComparisonOperator;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;
import dev.endoy.minecraft.helpers.database.definitions.WhereDefinition;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class WhereBuilder
{

    private final WhereGroupBuilder whereGroupBuilder;
    private String column;
    private SqlComparisonOperator operator;
    private List<SqlValue> values;

    public WhereBuilder equals( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.EQUALS;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder notEquals( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.NOT_EQUALS;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder greaterThan( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.GREATER_THAN;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder greaterThanOrEquals( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.GREATER_THAN_OR_EQUALS;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder lessThan( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.LESS_THAN;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder lessThanOrEquals( String column, SqlValue value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.LESS_THAN_OR_EQUALS;
        this.values = List.of( value );
        return this;
    }

    public WhereBuilder between( String column, SqlValue value1, SqlValue value2 )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.BETWEEN;
        this.values = List.of( value1, value2 );
        return this;
    }

    public WhereBuilder like( String column, String value )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.LIKE;
        this.values = List.of( SqlValue.literal( value ) );
        return this;
    }

    public WhereBuilder in( String column, SqlValue... values )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.IN;
        this.values = List.of( values );
        return this;
    }

    public WhereBuilder isNull( String column )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.IS_NULL;
        return this;
    }

    public WhereBuilder isNotNull( String column )
    {
        this.column = column;
        this.operator = SqlComparisonOperator.IS_NOT_NULL;
        return this;
    }

    public WhereBuilder and()
    {
        return whereGroupBuilder.addWhere( new WhereDefinition( column, operator, values ) ).and();
    }

    public WhereBuilder or()
    {
        return whereGroupBuilder.addWhere( new WhereDefinition( column, operator, values ) ).or();
    }

    public WhereBuilder startGroup()
    {
        return whereGroupBuilder.addWhere( new WhereDefinition( column, operator, values ) ).startGroup();
    }

    public WhereGroupBuilder endGroup()
    {
        return whereGroupBuilder.addWhere( new WhereDefinition( column, operator, values ) ).endGroup();
    }

    public SelectQueryBuilder end()
    {
        return whereGroupBuilder.addWhere( new WhereDefinition( column, operator, values ) ).end();
    }
}
