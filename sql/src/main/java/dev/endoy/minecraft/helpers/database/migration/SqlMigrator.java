package dev.endoy.minecraft.helpers.database.migration;

import dev.endoy.minecraft.helpers.database.DialectType;
import dev.endoy.minecraft.helpers.database.definitions.ColumnDataType;
import dev.endoy.minecraft.helpers.database.definitions.Selection;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;
import dev.endoy.minecraft.helpers.database.query.QueryBuilder;
import dev.endoy.minecraft.helpers.utils.ReflectionUtils;
import lombok.Builder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Builder
public class SqlMigrator
{

    private final DataSource dataSource;
    private final Class<?> initMigrationClass;
    private final DialectType dialectType;

    public SqlMigrator load()
    {
        if ( dataSource == null )
        {
            throw new MigrationException( "DataSource is not set" );
        }
        if ( initMigrationClass == null )
        {
            throw new MigrationException( "InitMigrationClass is not set" );
        }
        if ( dialectType == null )
        {
            throw new MigrationException( "DialectType is not set" );
        }

        try ( Connection connection = dataSource.getConnection() )
        {
            connection.setAutoCommit( false );

            try ( Statement statement = connection.createStatement() )
            {
                statement.execute(
                    QueryBuilder.forDialect( dialectType )
                        .createTable()
                        .ifNotExists()
                        .name( "migrations" )
                        .addColumn().name( "id" ).type( ColumnDataType.SERIAL ).addConstraint().primaryKey()
                        .and()
                        .addColumn().name( "version" ).type( ColumnDataType.INT ).addConstraint().notNull()
                        .and()
                        .addColumn().name( "name" ).type( ColumnDataType.TEXT ).addConstraint().notNull()
                        .and()
                        .addColumn().name( "created_at" ).type( ColumnDataType.DATETIME ).addConstraint().notNull().defaultValue( SqlValue.currentTimestamp() )
                        .and()
                        .addColumn().name( "success" ).type( ColumnDataType.BOOLEAN ).addConstraint().notNull().defaultValue( SqlValue.literal( true ) )
                        .and()
                        .build()
                );
            }

            connection.commit();
        }
        catch ( SQLException e )
        {
            throw new MigrationException( e );
        }
        return this;
    }

    public void migrate()
    {
        ReflectionUtils.getClassesInPackageImplementing( this.initMigrationClass, Migration.class )
            .stream()
            .map( ReflectionUtils::createInstance )
            .map( Migration.class::cast )
            .filter( Objects::nonNull )
            .forEach( migration ->
            {
                try ( Connection connection = dataSource.getConnection() )
                {
                    connection.setAutoCommit( false );
                    migration.migrate( connection );
                    connection.commit();
                }
                catch ( SQLException e )
                {
                    throw new MigrationException( e );
                }
            } );
    }

    private boolean shouldMigrate( Connection connection, String migrationName ) throws SQLException
    {
        try ( Statement statement = connection.createStatement() )
        {
            QueryBuilder.forDialect( dialectType )
                .select()
                .columns( Selection.count() )
                .from( "migrations" )
                .where().equals( "name", SqlValue.literal( migrationName ) )
//                .and().between( "success", SqlValue.literal( true ), SqlValue.literal( false ) )
//                .or().isNull( "success" )
//                .startGroup()
//                .isNotNull( "success" )
//                .end()
                .end()
                .build();

            // TODO: rigorously test this
            // TODO: maybe split up WhereBuilder into a WhereBuilder and ChildWhereBuilder (ChildWhereBuilder should only have .endGroup and .startGroup methods (or at least no .end that goes back to the select query builder)
            // TODO: maybe also have something similar with the group builder, a la ChildWhereGroupBuilder

//            return statement.executeQuery( "SELECT COUNT(*) FROM migrations WHERE name = '" + migrationName + "'" ).getInt( 1 ) == 0;
        }
    }
}
