package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.injector.annotations.Inject;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

public class MultipleInstancesExtendingAbstractClassInjectionTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test injectables implementing interface are injected correctly when there are two implementations" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of( TestComponent.class, TestComponent2.class, TestComponent3.class ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertEquals(
                TestComponent.class, injector.getInjectableInstance( TestComponent3.class ).implementingComponent.getClass()
            );
            assertEquals(
                TestComponent.class, injector.getInjectableInstance( IComponent.class ).getClass()
            );
        }
    }

    private static abstract class IComponent
    {
    }

    @Component
    public static class TestComponent extends IComponent
    {
    }

    @Component
    public static class TestComponent2 extends IComponent
    {
    }

    @Component
    public static class TestComponent3
    {

        @Inject
        IComponent implementingComponent;

    }
}
