package dev.endoy.minecraft.helpers.database.query.select;

import dev.endoy.minecraft.helpers.database.definitions.SqlOperator;
import dev.endoy.minecraft.helpers.database.definitions.WhereDefinition;
import dev.endoy.minecraft.helpers.database.definitions.WhereGroupDefinition;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class WhereGroupBuilder
{

    private final SelectQueryBuilder selectQueryBuilder;
    private final WhereGroupBuilder whereGroupBuilder;
    private List<WhereGroupDefinition> groups = new ArrayList<>();
    private List<WhereDefinition> wheres = new ArrayList<>();
    private SqlOperator operator;

    public WhereBuilder startGroup()
    {
        return new WhereBuilder( new WhereGroupBuilder( selectQueryBuilder, this ) );
    }

    public WhereBuilder and()
    {
        this.operator = SqlOperator.AND;
        return new WhereBuilder( this );
    }

    public WhereBuilder or()
    {
        this.operator = SqlOperator.OR;
        return new WhereBuilder( this );
    }

    public WhereGroupBuilder addWhere( WhereDefinition whereDefinition )
    {
        wheres.add( whereDefinition );
        return this;
    }

    public WhereGroupBuilder addWhereGroup( WhereGroupDefinition whereGroupDefinition )
    {
        groups.add( whereGroupDefinition );
        return this;
    }

    public WhereGroupBuilder endGroup()
    {
        if ( whereGroupBuilder == null )
        {
            return this;
        }

        whereGroupBuilder.addWhereGroup( new WhereGroupDefinition(
            this.operator,
            this.groups,
            this.wheres
        ) );
        return whereGroupBuilder;
    }

    public SelectQueryBuilder end()
    {
        selectQueryBuilder.addWhereGroup( new WhereGroupDefinition(
            this.operator,
            this.groups,
            this.wheres
        ) );
        return selectQueryBuilder;
    }
}
