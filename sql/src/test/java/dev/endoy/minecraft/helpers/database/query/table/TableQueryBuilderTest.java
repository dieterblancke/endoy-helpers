package dev.endoy.minecraft.helpers.database.query.table;

import dev.endoy.minecraft.helpers.database.DialectType;
import dev.endoy.minecraft.helpers.database.definitions.ColumnDataType;
import dev.endoy.minecraft.helpers.database.definitions.SqlValue;
import dev.endoy.minecraft.helpers.database.query.QueryBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableQueryBuilderTest
{

    @Test
    @DisplayName( "Test create table query with a simple table" )
    void testCreateTableQuery()
    {
        String query = QueryBuilder.forDialect( DialectType.MYSQL )
            .createTable()
            .name( "test_table" )
            .addColumn()
            .name( "id" ).type( ColumnDataType.SERIAL )
            .addConstraint().primaryKey()
            .and()
            .addColumn().name( "name" ).type( ColumnDataType.VARCHAR ).maxLength( 255 )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .build();

        assertEquals( "CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE);", query );
    }

    @Test
    @DisplayName( "Test create table query with a foreign key" )
    void testCreateTableQueryWithForeignKey()
    {
        String query = QueryBuilder.forDialect( DialectType.MYSQL )
            .createTable()
            .name( "test_table" )
            .addColumn()
            .name( "id" ).type( ColumnDataType.SERIAL )
            .addConstraint().primaryKey()
            .and()
            .addColumn().name( "name" ).type( ColumnDataType.VARCHAR ).maxLength( 255 )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "other_table_id" ).type( ColumnDataType.INT )
            .addConstraint().foreignKey( "other_table", "id" )
            .and()
            .build();

        assertEquals( "CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE, other_table_id INT, FOREIGN KEY (other_table_id) REFERENCES other_table(id));", query );
    }

    @Test
    @DisplayName( "Test create table with size and decimal counts, literal default values and current timestamp default value" )
    void testCreateTableQueryWithSizeAndDecimalCountsAndDefaultValues()
    {
        String query = QueryBuilder.forDialect( DialectType.MYSQL )
            .createTable()
            .name( "test_table" )
            .addColumn()
            .name( "id" ).type( ColumnDataType.SERIAL )
            .addConstraint().primaryKey()
            .and()
            .addColumn().name( "name" ).type( ColumnDataType.VARCHAR ).maxLength( 255 )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "price" ).type( ColumnDataType.DECIMAL ).maxLength( 10 ).decimalSize( 2 )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "is_active" ).type( ColumnDataType.BOOLEAN ).defaultValue( SqlValue.literal( true ) )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "created_at" ).type( ColumnDataType.TIMESTAMP )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "updated_at" ).type( ColumnDataType.TIMESTAMP )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "deleted_at" ).type( ColumnDataType.TIMESTAMP )
            .addConstraint().notNull().addConstraint().unique()
            .and()
            .addColumn().name( "default_value" ).type( ColumnDataType.VARCHAR ).maxLength( 255 )
            .addConstraint().notNull().defaultValue( SqlValue.literal( "default" ) )
            .and()
            .addColumn().name( "current_timestamp" ).type( ColumnDataType.TIMESTAMP ).defaultValue( SqlValue.currentTimestamp() )
            .addConstraint().notNull()
            .and()
            .build();

        assertEquals( "CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE, price DECIMAL(10,2) NOT NULL UNIQUE, is_active BOOLEAN NOT NULL UNIQUE DEFAULT 1, created_at TIMESTAMP NOT NULL UNIQUE, updated_at TIMESTAMP NOT NULL UNIQUE, deleted_at TIMESTAMP NOT NULL UNIQUE, default_value VARCHAR(255) NOT NULL DEFAULT 'default', current_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);", query );
    }
}