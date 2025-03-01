package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.injector.exceptions.CircularDependencyException;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

public class InjectableWithDeepCircularDependencyTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test circular dependency detection with self-reference from it's own constructor" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestComponentWithCircularDependency.class,
                    TestComponentWithCircularDependency2.class,
                    TestComponentWithCircularDependency3.class
                ) );
            TestHelper.callRealMethods( reflectionUtils );

            assertThrows( CircularDependencyException.class, () ->
            {
                Injector injector = Injector.forProject( this.getClass(), this );
                injector.inject();
            } );
        }
    }

    @Value
    @Component
    public static class TestComponentWithCircularDependency
    {

        TestComponentWithCircularDependency2 testComponent;

    }

    @Value
    @Component
    public static class TestComponentWithCircularDependency2
    {

        TestComponentWithCircularDependency3 testComponent;

    }

    @Value
    @Component
    public static class TestComponentWithCircularDependency3
    {

        TestComponentWithCircularDependency testComponent;

    }
}
