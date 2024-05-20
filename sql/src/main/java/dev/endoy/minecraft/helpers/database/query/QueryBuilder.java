package dev.endoy.minecraft.helpers.database.query;

import dev.endoy.minecraft.helpers.database.DialectType;
import dev.endoy.minecraft.helpers.database.SQLDialect;
import dev.endoy.minecraft.helpers.database.query.select.SelectQueryBuilder;
import dev.endoy.minecraft.helpers.database.query.table.TableQueryBuilder;

public class QueryBuilder
{

    private final SQLDialect dialect;

    private QueryBuilder( SQLDialect dialect )
    {
        this.dialect = dialect;
    }

    public static QueryBuilder forDialect( DialectType dialectType )
    {
        return new QueryBuilder( dialectType.getDialect() );
    }

    public TableQueryBuilder createTable()
    {
        return new TableQueryBuilder( dialect );
    }

    public SelectQueryBuilder select()
    {
        return new SelectQueryBuilder( dialect );
    }
}
