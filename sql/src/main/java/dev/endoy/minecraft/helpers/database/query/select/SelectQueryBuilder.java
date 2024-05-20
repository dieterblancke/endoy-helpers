package dev.endoy.minecraft.helpers.database.query.select;

import dev.endoy.minecraft.helpers.database.SQLDialect;
import dev.endoy.minecraft.helpers.database.definitions.Selection;
import dev.endoy.minecraft.helpers.database.definitions.WhereGroupDefinition;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class SelectQueryBuilder
{

    private final SQLDialect dialect;
    private List<Selection> columns = new ArrayList<>();
    private String tableName;
    private List<WhereGroupDefinition> whereGroups = new ArrayList<>();

    public SelectQueryBuilder column( Selection column )
    {
        columns.add( column );
        return this;
    }

    public SelectQueryBuilder columns( Selection... columns )
    {
        this.columns.addAll( Arrays.asList( columns ) );
        return this;
    }

    public SelectQueryBuilder columns( List<Selection> columns )
    {
        this.columns = columns;
        return this;
    }

    public SelectQueryBuilder from( String tableName )
    {
        this.tableName = tableName;
        return this;
    }

    public WhereBuilder where()
    {
        return new WhereBuilder( new WhereGroupBuilder( this, null ) );
    }

    public SelectQueryBuilder addWhereGroup( WhereGroupDefinition whereGroup )
    {
        this.whereGroups.add( whereGroup );
        return this;
    }

    public SelectQueryBuilder addWhereGroups( List<WhereGroupDefinition> whereGroups )
    {
        this.whereGroups.addAll( whereGroups );
        return this;
    }

    public String build()
    {
//        return dialect.select( columns, tableName, whereGroups ); TODO
        return null;
    }
}
