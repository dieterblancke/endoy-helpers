package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.injector.annotations.PostConstruct;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class InjectableWithPostConstructTest extends EndoyApplicationTest
{

    private static boolean postConstruct1Called = false;
    private static boolean postConstruct2Called = false;

    @Test
    @DisplayName( "Test PostConstruct to be functional" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestComponent.class,
                    TestComponent2.class
                ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertTrue( postConstruct1Called );
            assertTrue( postConstruct2Called );
        }
    }

    @Value
    @Component
    public static class TestComponent
    {

        TestComponent2 testComponent2;

        @PostConstruct
        void postConstruct()
        {
            postConstruct1Called = true;
        }
    }

    @Component
    public static class TestComponent2
    {

        @PostConstruct
        void postConstruct()
        {
            postConstruct2Called = true;
        }
    }
}