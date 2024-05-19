package dev.endoy.minecraft.helpers.injector.withoutcirculardeps;

import dev.endoy.minecraft.helpers.EndoyApplicationTest;
import dev.endoy.minecraft.helpers.injector.Component;
import dev.endoy.minecraft.helpers.injector.Injector;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InjectableWithoutCircularDepsTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test normal injection to be functional" )
    void testInject()
    {
        Injector injector = Injector.forProject( this.getClass(), this );
        injector.inject();

        assertEquals( injector.getInjectableInstance( TestComponentWithoutCircularDependency.class ).getTestComponent2().getNumber(), 1 );
        assertEquals( injector.getInjectableInstance( TestComponentWithoutCircularDependency2.class ).getNumber(), 1 );
    }

    @Value
    @Component
    public static class TestComponentWithoutCircularDependency
    {

        TestComponentWithoutCircularDependency2 testComponent2;

    }

    @Component
    public static class TestComponentWithoutCircularDependency2
    {

        public int getNumber()
        {
            return 1;
        }
    }
}