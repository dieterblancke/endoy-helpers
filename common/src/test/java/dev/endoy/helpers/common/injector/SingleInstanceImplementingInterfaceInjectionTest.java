package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class SingleInstanceImplementingInterfaceInjectionTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test injectables implementing interface are injected correctly when there are two implementations" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of( TestComponent.class, TestComponent2.class ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertEquals(
                TestComponent.class, injector.getInjectableInstance( TestComponent2.class ).implementingComponent.getClass()
            );
            assertEquals(
                TestComponent.class, injector.getInjectableInstance( IComponent.class ).getClass()
            );
        }
    }

    private interface IComponent
    {
    }

    @Component
    public static class TestComponent implements IComponent
    {
    }

    @Component
    public static class TestComponent2
    {

        @Inject
        IComponent implementingComponent;

    }
}
