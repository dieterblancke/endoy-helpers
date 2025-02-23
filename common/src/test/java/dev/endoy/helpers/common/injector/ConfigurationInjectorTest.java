package dev.endoy.helpers.common.injector;

import dev.endoy.helpers.common.EndoyApplicationTest;
import dev.endoy.helpers.common.TestHelper;
import dev.endoy.helpers.common.transform.TransformValue;
import dev.endoy.helpers.common.transform.ValueTransformer;
import dev.endoy.helpers.common.utils.ReflectionUtils;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

class ConfigurationInjectorTest extends EndoyApplicationTest
{

    @BeforeAll
    public static void setup() throws IOException
    {
        dataFolder = TestHelper.createTempDirectory();

        Files.writeString(
            new File( dataFolder, "config.yml" ).toPath(),
            """
                test: test
                test-section:
                  test: test
                  test-nr: 123
                """
        );

        Files.writeString(
            new File( dataFolder, "config-with-comments.yml" ).toPath(),
            """
                # This is a test
                test: test
                # This is a test section
                # With a second line
                # A third line
                # And even a fourth line :O
                test-section:
                  # This is a test section with test comment
                  test: test
                  # This is a test section with test number comment
                  # Even with multi line comments!!!1!!
                  test-nr: 123"""
        );
        Files.writeString(
            new File( dataFolder, "config-with-enum.yml" ).toPath(),
            """
                test: TEST
                """
        );
        Files.writeString(
            new File( dataFolder, "config-with-transform.yml" ).toPath(),
            """
                test: testing
                """
        );
        Files.writeString(
            new File( dataFolder, "config-with-map.yml" ).toPath(),
            """
                test:
                  test: test
                  test-nr: 123
                """
        );
    }

    @Test
    void testCreateDefault()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestConfiguration.class,
                    TestConfiguration.TestConfigurationSection.class,
                    TestConfigurationWithComments.class,
                    TestConfigurationWithComments.TestConfigurationSectionWithComments.class,
                    TestConfigurationWithEnum.class,
                    TestConfigurationWithTransform.class,
                    TestTransform.class,
                    TransformedTest.class,
                    TestEnum.class
                ) );

            TestHelper.callRealMethods( reflectionUtils );

            Injector injector = Injector.forProject( this.getClass(), this );
            this.setInjector( injector );
            injector.inject();

            TestConfiguration testConfiguration = injector.getInjectableInstance( TestConfiguration.class );
            assertEquals( "test", testConfiguration.getTest() );
            assertEquals( "test", testConfiguration.getTestSection().getTest() );
            assertEquals( 123, testConfiguration.getTestSection().getTestNr() );
        }
    }

    @Test
    void testCreateDefaultWithComments()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestConfiguration.class,
                    TestConfiguration.TestConfigurationSection.class,
                    TestConfigurationWithComments.class,
                    TestConfigurationWithComments.TestConfigurationSectionWithComments.class,
                    TestConfigurationWithEnum.class,
                    TestConfigurationWithTransform.class,
                    TestTransform.class,
                    TransformedTest.class,
                    TestEnum.class
                ) );

            TestHelper.callRealMethods( reflectionUtils );
            Injector injector = Injector.forProject( this.getClass(), this );
            this.setInjector( injector );
            injector.inject();

            TestConfigurationWithComments testConfiguration = injector.getInjectableInstance( TestConfigurationWithComments.class );
            assertEquals( "test", testConfiguration.getTest() );
            assertEquals( "test", testConfiguration.getTestSection().getTest() );
            assertEquals( 123, testConfiguration.getTestSection().getTestNr() );
        }
    }

    @Test
    void testWithEnum()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestConfiguration.class,
                    TestConfiguration.TestConfigurationSection.class,
                    TestConfigurationWithComments.class,
                    TestConfigurationWithComments.TestConfigurationSectionWithComments.class,
                    TestConfigurationWithEnum.class,
                    TestConfigurationWithTransform.class,
                    TestTransform.class,
                    TransformedTest.class,
                    TestEnum.class
                ) );

            TestHelper.callRealMethods( reflectionUtils );
            Injector injector = Injector.forProject( this.getClass(), this );
            this.setInjector( injector );
            injector.inject();

            TestConfigurationWithEnum testConfiguration = injector.getInjectableInstance( TestConfigurationWithEnum.class );
            assertEquals( TestEnum.TEST, testConfiguration.getTest() );
        }
    }

    @Test
    void testWithTransform()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestConfiguration.class,
                    TestConfiguration.TestConfigurationSection.class,
                    TestConfigurationWithComments.class,
                    TestConfigurationWithComments.TestConfigurationSectionWithComments.class,
                    TestConfigurationWithEnum.class,
                    TestConfigurationWithTransform.class,
                    TestTransform.class,
                    TransformedTest.class,
                    TestEnum.class
                ) );

            TestHelper.callRealMethods( reflectionUtils );
            Injector injector = Injector.forProject( this.getClass(), this );
            this.setInjector( injector );
            injector.inject();

            TestConfigurationWithTransform testConfiguration = injector.getInjectableInstance( TestConfigurationWithTransform.class );
            assertEquals( "testing", testConfiguration.getTest().getValue() );
        }
    }

    @Test
    void testWithMap()
    {
        try ( MockedStatic<ReflectionUtils> reflectionUtils = mockStatic( ReflectionUtils.class ) )
        {
            reflectionUtils.when( () -> ReflectionUtils.getClassesInPackage( this.getClass() ) )
                .thenReturn( List.of(
                    TestConfigurationWithMap.class
                ) );

            TestHelper.callRealMethods( reflectionUtils );
            Injector injector = Injector.forProject( this.getClass(), this );
            this.setInjector( injector );
            injector.inject();

            TestConfigurationWithMap testConfiguration = injector.getInjectableInstance( TestConfigurationWithMap.class );
            assertEquals( 2, testConfiguration.getTest().size() );
            assertEquals( "test", testConfiguration.getTest().get( "test" ) );
            assertEquals( 123, testConfiguration.getTest().get( "test-nr" ) );
        }
    }

    public enum TestEnum
    {
        TEST, TESTING
    }

    @Getter
    @Configuration( filePath = "config.yml" )
    public static class TestConfiguration
    {

        @Value
        private final String test = "test";

        @Value( path = "test-section" )
        private final TestConfigurationSection testSection = new TestConfigurationSection();

        @Getter
        @ConfigurationSection
        public static class TestConfigurationSection
        {

            @Value
            private final String test = "test";

            @Value( path = "test-nr" )
            private final int testNr = 123;

        }
    }

    @Getter
    @Configuration( filePath = "config-with-comments.yml" )
    public static class TestConfigurationWithComments
    {

        @Value
        @Comment( "This is a test" )
        private final String test = "test";

        @Value( path = "test-section" )
        @Comment( { "This is a test section", "With a second line", "A third line", "And even a fourth line :O" } )
        private final TestConfigurationSectionWithComments testSection = new TestConfigurationSectionWithComments();

        @Getter
        @ConfigurationSection
        public static class TestConfigurationSectionWithComments
        {

            @Value
            @Comment( "This is a test section with test comment" )
            private final String test = "test";

            @Value( path = "test-nr" )
            @Comment( { "This is a test section with test number comment", "Even with multi line comments!!!1!!" } )
            private final int testNr = 123;

        }
    }

    @Getter
    @Configuration( filePath = "config-with-enum.yml" )
    public static class TestConfigurationWithEnum
    {

        @Value
        private final TestEnum test = TestEnum.TEST;

    }

    @Getter
    @Configuration( filePath = "config-with-transform.yml" )
    public static class TestConfigurationWithTransform
    {

        @Value
        @TransformValue( value = TestTransform.class )
        private final TransformedTest test = new TransformedTest( "testing" );

    }

    @Getter
    @Configuration( filePath = "config-with-map.yml" )
    public static class TestConfigurationWithMap
    {

        @Value
        private final Map<String, Object> test = new HashMap<>();

    }

    public static class TestTransform implements ValueTransformer<TransformedTest>
    {

        @Override
        public TransformedTest transformFromConfigValue( Object value )
        {
            return new TransformedTest( (String) value );
        }

        @Override
        public Object transformToConfigValue( TransformedTest value )
        {
            return value.getValue();
        }
    }

    public static class TransformedTest
    {
        @Getter
        private final String value;

        public TransformedTest( String value )
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }
    }
}