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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

public class InjectableWithDeepCircularDependencyTest2 extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test circular dependency detection with nested non-circular dependencies." )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of( A.class, B.class, C.class ) );
            TestHelper.callRealMethods( reflectionUtils );

            assertDoesNotThrow( () ->
            {
                Injector injector = Injector.forProject( this.getClass(), this );
                injector.inject();
            } );
        }
    }

    @Value
    @Component
    public static class A // could be a Config (see #13)
    {
    }

    @Value
    @Component
    public static class B
    {

        A a;
        C c;

    }

    @Value
    @Component
    public static class C
    {

        A a;

    }
}
