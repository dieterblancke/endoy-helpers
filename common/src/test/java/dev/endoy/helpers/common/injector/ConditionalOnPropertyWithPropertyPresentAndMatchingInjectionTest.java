package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.injector.annotations.Component;
import dev.endoy.helpers.common.injector.annotations.ConditionalOnConfigProperty;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

public class ConditionalOnPropertyWithPropertyPresentAndMatchingInjectionTest extends EndoyApplicationTest
{

    @BeforeAll
    public static void setup() throws IOException
    {
        dataFolder = TestHelper.createTempDirectory();

        Files.writeString(
            new File( dataFolder, "config.yml" ).toPath(),
            """
                test:
                  property: true
                """
        );
    }

    @Test
    @DisplayName( "Test injection when ConditionalOnProperty is present and matches the property value" )
    void testInject()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestComponent.class
                ) );
            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            injector.inject();

            assertNotNull( injector.getInjectableInstance( TestComponent.class ) );
        }
    }

    @Component
    @ConditionalOnConfigProperty( propertyPath = "test.property", havingValue = "true" )
    public static class TestComponent
    {
    }
}
