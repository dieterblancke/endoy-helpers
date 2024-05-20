package dev.endoy.minecraft.helpers.database.definitions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ColumnDefinition
{

    private final String name;
    private final ColumnDataType dataType;
    private final int maxLength;
    private final int decimalSize;
    private final List<ConstraintDefinition> constraints;
    private final SqlValue defaultSqlValue;

    public ColumnDefinition( String name, ColumnDataType dataType, int maxLength )
    {
        this( name, dataType, maxLength, -1, null, null );
    }

    public ColumnDefinition( String name, ColumnDataType dataType, int maxLength, int decimalSize )
    {
        this( name, dataType, maxLength, -1, null, null );
    }
}
