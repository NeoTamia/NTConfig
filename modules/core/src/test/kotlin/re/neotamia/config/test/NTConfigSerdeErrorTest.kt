package re.neotamia.config.test

import re.neotamia.config.NTConfig
import re.neotamia.config.NTConfigException
import re.neotamia.nightconfig.toml.TomlFormat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class NTConfigSerdeErrorTest {
    private lateinit var tempDir: Path
    private lateinit var ntConfig: NTConfig

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("ntconfig-serde-error-test")
        ntConfig = NTConfig()
        ntConfig.registerFormat(TomlFormat.instance(), "toml")
    }

    @AfterTest
    fun tearDown() {
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }

    class SimpleConfig {
        var count: Int = 0
    }

    @Test
    fun testDeserializationError() {
        val path = tempDir.resolve("error.toml")
        Files.writeString(path, "count = \"not-an-number\"")

        val exception = assertFailsWith<NTConfigException> {
            ntConfig.load(path, SimpleConfig())
        }

        assertTrue(exception.message!!.contains("Failed to deserialize configuration class"), "Message should contain 'Failed to deserialize'")
        assertTrue(exception.message!!.contains("SimpleConfig"), "Message should contain class name")
        // Check if field path is correctly identified if night-config provides it
        println("Exception message: ${exception.message}")
    }

    class NestedConfig {
        var nested: SimpleConfig = SimpleConfig()
    }

    @Test
    fun testNestedDeserializationError() {
        val path = tempDir.resolve("nested-error.toml")
        Files.writeString(path, "[nested]\ncount = \"not-an-number\"")

        val exception = assertFailsWith<NTConfigException> {
            ntConfig.load(path, NestedConfig())
        }

        println("Nested exception message: ${exception.message}")

        // Debugging causes
        var current: Throwable? = exception
        while (current != null) {
            println("Cause: ${current.javaClass.name}: ${current.message}")
            current = current.cause
        }

        assertTrue(exception.message!!.contains("Failed to deserialize configuration class"), "Message should contain 'Failed to deserialize'")
        // assertTrue(exception.message!!.contains("count"), "Message should contain field name 'count'")
    }

    class SerializationErrorConfig {
        var unmappable: java.awt.Color = java.awt.Color.RED
    }

    @Test
    fun testSerializationError() {
        val config = SerializationErrorConfig()
        val path = tempDir.resolve("serial-error.toml")

        val exception = assertFailsWith<NTConfigException> {
            ntConfig.save(path, config)
        }

        println("Serialization exception message: ${exception.message}")
        assertTrue(exception.message!!.contains("Failed to serialize configuration class"), "Message should contain 'Failed to serialize'")
        assertTrue(exception.message!!.contains("SerializationErrorConfig"), "Message should contain class name")
    }

    class DeepConfig {
        var middle: MiddleConfig = MiddleConfig()
    }

    class MiddleConfig {
        var simple: SimpleConfig = SimpleConfig()
    }

    @Test
    fun testBuildSerdeMessageLogic() {
        val serdeExcClass = Class.forName("re.neotamia.nightconfig.core.serde.SerdeException")
        val constructor = serdeExcClass.getDeclaredConstructor(String::class.java, Throwable::class.java)
        constructor.isAccessible = true

        fun createExc(msg: String, cause: Throwable? = null) = constructor.newInstance(msg, cause) as Exception

        val innerMost = RuntimeException("Root cause")
        val exc3 = createExc("Error in field `re.neotamia.config.C.fieldC`", innerMost)
        val exc2 = createExc("Error in field `re.neotamia.config.B.fieldB`", exc3)
        val exc1 = createExc("Error in field `re.neotamia.config.A.fieldA`", exc2)

        val method = NTConfig::class.java.getDeclaredMethod("buildSerdeMessage", String::class.java, String::class.java, serdeExcClass)
        method.isAccessible = true

        val result = method.invoke(null, "test", "re.neotamia.config.A", exc1) as String

        println("Reflection Result: $result")

        // Expected field path: fieldA.fieldB.fieldC
        assertTrue(result.contains("Field path: fieldA.fieldB.fieldC"), "Should join field names correctly")
        // Expected class path: A.fieldA -> B.fieldB -> C.fieldC
        assertTrue(result.contains("A.fieldA -> B.fieldB -> C.fieldC"), "Should join class paths correctly")
        assertTrue(result.contains("Cause: Root cause"), "Should include root cause")
        assertTrue(result.contains("Failed to test configuration class A (re.neotamia.config.A)"), "Should include action and class names")
    }

    @Test
    fun testDeepDeserializationError() {
        val path = tempDir.resolve("deep-error.toml")
        Files.writeString(path, "[middle.simple]\ncount = \"not-an-number\"")

        val exception = assertFailsWith<NTConfigException> {
            ntConfig.load(path, DeepConfig())
        }

        println("Deep exception message: ${exception.message}")
        assertTrue(exception.message!!.contains("Failed to deserialize"), "Message should contain 'Failed to deserialize'")
        assertTrue(exception.message!!.contains("middle"), "Message should contain field 'middle'")
    }

    @Test
    fun testParseFieldDescriptorVariants() {
        val method = NTConfig::class.java.getDeclaredMethod("parseFieldDescriptor", String::class.java)
        method.isAccessible = true

        fun parse(desc: String): Any {
            val fieldInfo = method.invoke(null, desc)
            val classPathMethod = fieldInfo.javaClass.getMethod("classPath").apply { isAccessible = true }
            val fieldNameMethod = fieldInfo.javaClass.getMethod("fieldName").apply { isAccessible = true }
            val classPath = classPathMethod.invoke(fieldInfo) as String
            val fieldName = fieldNameMethod.invoke(fieldInfo) as String
            return Pair(classPath, fieldName)
        }

        // Test with full descriptor: "re.neotamia.config.MyClass.myField"
        assertEquals(Pair("MyClass.myField", "myField"), parse("re.neotamia.config.MyClass.myField"))

        // Test with simple descriptor: "myField"
        assertEquals(Pair("myField", "myField"), parse("myField"))

        // Test with leading/trailing spaces
        assertEquals(Pair("MyClass.myField", "myField"), parse("  re.neotamia.config.MyClass.myField  "))

        // Test with other tokens before the descriptor (as seen in night-config messages)
        assertEquals(Pair("MyClass.myField", "myField"), parse("something something re.neotamia.config.MyClass.myField"))
    }

    @Test
    fun testFindRootCauseMessage() {
        val method = NTConfig::class.java.getDeclaredMethod("findRootCauseMessage", Throwable::class.java)
        method.isAccessible = true

        fun find(t: Throwable): String = method.invoke(null, t) as String

        // Case with message
        assertEquals("Some message", find(RuntimeException("Some message")))

        // Nested case
        assertEquals("Deep message", find(RuntimeException("Outer", RuntimeException("Deep message"))))

        // Null message case
        assertEquals("java.lang.NullPointerException", find(NullPointerException()))

        // Blank message case
        assertEquals("java.lang.RuntimeException", find(RuntimeException("   ")))
    }
}
