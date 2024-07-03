package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

public class ConditionalOnPropertyWithPropertyPresentAndNotMatchingInjectionTest extends EndoyApplicationTest
{

    @BeforeAll
    public static void setup() throws IOException
    {
        dataFolder = TestHelper.createTempDirectory();

        Files.writeString(
            new File( dataFolder, "config.yml" ).toPath(),
            """
                test:
                  property: false
                """
        );
    }

    @Test
    @DisplayName( "Test injectables implementing interface are injected correctly when there are two implementations" )
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

            assertNull( injector.getInjectableInstance( TestComponent.class ) );
        }
    }

    @Component
    @ConditionalOnConfigProperty( propertyPath = "test.property", havingValue = "true" )
    public static class TestComponent
    {
    }
}
