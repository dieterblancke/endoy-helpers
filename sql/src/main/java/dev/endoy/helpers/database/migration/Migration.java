package dev.endoy.helpers.database.migration;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration
{

    void migrate( Connection connection ) throws SQLException;

}