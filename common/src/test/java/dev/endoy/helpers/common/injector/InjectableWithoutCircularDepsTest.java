package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class InjectableWithoutCircularDepsTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test normal injection to be functional" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestComponentWithoutCircularDependency.class,
                    TestComponentWithoutCircularDependency2.class
                ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertEquals( injector.getInjectableInstance( TestComponentWithoutCircularDependency.class ).getTestComponent2().getNumber(), 1 );
            assertEquals( injector.getInjectableInstance( TestComponentWithoutCircularDependency2.class ).getNumber(), 1 );
        }
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