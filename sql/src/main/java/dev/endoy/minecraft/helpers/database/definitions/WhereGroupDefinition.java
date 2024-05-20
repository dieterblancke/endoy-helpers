package dev.endoy.minecraft.helpers.database.definitions;

import lombok.Value;

import java.util.List;

@Value
public class WhereGroupDefinition
{

    SqlOperator operator;
    List<WhereGroupDefinition> whereGroups;
    List<WhereDefinition> wheres;

}
