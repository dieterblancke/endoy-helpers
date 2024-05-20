package dev.endoy.minecraft.helpers.database.definitions;

import lombok.Value;

import java.util.List;

@Value
public class WhereDefinition
{

    String column;
    SqlComparisonOperator operator;
    List<SqlValue> values;

}
