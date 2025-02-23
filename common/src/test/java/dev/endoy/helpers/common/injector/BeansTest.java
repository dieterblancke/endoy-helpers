package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

class BeansTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test PostConstruct to be functional" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    BeanTest.class,
                    TestBeanComponent.class,
                    TestComponent.class
                ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertNotNull( injector.getInjectableInstance( TestBeanComponent.class ) );
            assertNotNull( injector.getInjectableInstance( TestComponent.class ) );
            assertNotNull( injector.getInjectableInstance( TestComponent.class ).getTestBeanComponent() );
            assertEquals(
                injector.getInjectableInstance( TestComponent.class ).getTestBeanComponent(),
                injector.getInjectableInstance( TestBeanComponent.class )
            );
        }
    }

    @Beans
    public static class BeanTest
    {
        @Bean
        TestBeanComponent testComponent()
        {
            return new TestBeanComponent();
        }
    }

    public static class TestBeanComponent
    {
    }

    @Component
    @Getter
    @RequiredArgsConstructor
    public static class TestComponent
    {
        private final TestBeanComponent testBeanComponent;
    }
}