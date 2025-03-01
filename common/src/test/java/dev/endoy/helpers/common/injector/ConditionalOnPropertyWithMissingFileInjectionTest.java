package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.injector.annotations.ConditionalOnConfigProperty;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

public class ConditionalOnPropertyWithMissingFileInjectionTest extends EndoyApplicationTest
{

    @Test
    @DisplayName( "Test injection when ConditionalOnProperty is present but the file is missing" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of( TestComponent.class ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertNull( injector.getInjectableInstance( TestComponent.class ) );
        }
    }

    @Component
    @ConditionalOnConfigProperty( propertyPath = "test.property", havingValue = "true" )
    public static class TestComponent
    {
    }
}
