package re.neotamia.config.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.annotation.ConfigHeader
import re.neotamia.config.annotation.ConfigProperty
import re.neotamia.config.annotation.SerializedName
import re.neotamia.config.json.JsonSerializer
import re.neotamia.config.naming.NamingStrategy
import re.neotamia.config.toml.TomlSerializer
import re.neotamia.config.yaml.YamlSerializer
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigHeaderAndNamingTest {

    @ConfigHeader("This is a test configuration file.\nGenerated for testing purposes.")
    class TestConfigWithHeader {
        var serverName: String = "test-server"
        var portNumber: Int = 8080
        var enableDebug: Boolean = true

        @SerializedName("custom_field")
        var fieldWithCustomName: String = "explicit-name"

        @ConfigProperty(name = "another_custom")
        var anotherField: String = "another-explicit"
    }

    class TestConfigForNaming {
        var serverName: String = "test-server"
        var portNumber: Int = 8080
        var enableDebug: Boolean = true
        var maxConnectionCount: String = "100"

        @SerializedName("custom_field")
        var fieldWithExplicitName: String = "should-keep-explicit-name"

        @ConfigProperty(name = "config_prop_name")
        var fieldWithConfigProperty: String = "should-keep-config-prop-name"
    }

    private fun nt(): NTConfig {
        val nt = NTConfig()
        nt.registerSerializer(YamlSerializer())
        nt.registerSerializer(JsonSerializer())
        nt.registerSerializer(TomlSerializer())
        return nt
    }

    @Test
    fun test_config_header_yaml(@TempDir tmp: Path) {
        val config = TestConfigWithHeader()
        val file = tmp.resolve("test.yaml")

        nt().save(file, config)
        val content = file.readText()

        println("YAML with Header:")
        println(content)

        // Check header is present with proper formatting
        assertContains(content, "# This is a test configuration file.")
        assertContains(content, "# Generated for testing purposes.")

        // Check there's a line break between header and content
        assertTrue(content.contains("# Generated for testing purposes.\n\n"))

        // Check content is present
        assertContains(content, "serverName:")
        assertContains(content, "portNumber:")
        assertContains(content, "enableDebug:")
    }

    @Test
    fun test_config_header_toml(@TempDir tmp: Path) {
        val config = TestConfigWithHeader()
        val file = tmp.resolve("test.toml")

        nt().save(file, config)
        val content = file.readText()

        println("TOML with Header:")
        println(content)

        // Check header is present with proper formatting
        assertContains(content, "# This is a test configuration file.")
        assertContains(content, "# Generated for testing purposes.")

        // Check there's a line break between header and content
        assertTrue(content.contains("# Generated for testing purposes.\n\n"))

        // Check content is present
        assertContains(content, "serverName =")
        assertContains(content, "portNumber =")
        assertContains(content, "enableDebug =")
    }

    @Test
    fun test_config_header_json_no_comments(@TempDir tmp: Path) {
        val config = TestConfigWithHeader()
        val file = tmp.resolve("test.json")

        nt().save(file, config)
        val content = file.readText()

        println("JSON (no comments):")
        println(content)

        // JSON should not have comments
        assertFalse(content.contains("#"))

        // But should have content
        assertContains(content, "\"serverName\":")
        assertContains(content, "\"portNumber\":")
        assertContains(content, "\"enableDebug\":")
    }

    @Test
    fun test_snake_case_naming(@TempDir tmp: Path) {
        val config = TestConfigForNaming()
        val file = tmp.resolve("test.yaml")

        val nt = nt()
        nt.namingStrategy = NamingStrategy.SNAKE_CASE
        nt.save(file, config)

        val content = file.readText()
        println("Snake Case Naming:")
        println(content)

        // Check naming strategy applied
        assertContains(content, "server_name:")
        assertContains(content, "port_number:")
        assertContains(content, "enable_debug:")
        assertContains(content, "max_connection_count:")

        // Check explicit names preserved
        assertContains(content, "custom_field:") // from @SerializedName
        assertContains(content, "config_prop_name:") // from @ConfigProperty(name)

        // Should NOT contain original camelCase names
        assertFalse(content.contains("serverName:"))
        assertFalse(content.contains("portNumber:"))
        assertFalse(content.contains("enableDebug:"))
        assertFalse(content.contains("maxConnectionCount:"))
    }

    @Test
    fun test_kebab_case_naming(@TempDir tmp: Path) {
        val config = TestConfigForNaming()
        val file = tmp.resolve("test.yaml")

        val nt = nt()
        nt.namingStrategy = NamingStrategy.KEBAB_CASE
        nt.save(file, config)

        val content = file.readText()
        println("Kebab Case Naming:")
        println(content)

        // Check naming strategy applied
        assertContains(content, "server-name:")
        assertContains(content, "port-number:")
        assertContains(content, "enable-debug:")
        assertContains(content, "max-connection-count:")

        // Check explicit names preserved
        assertContains(content, "custom_field:") // from @SerializedName
        assertContains(content, "config_prop_name:") // from @ConfigProperty(name)
    }

    @Test
    fun test_pascal_case_naming(@TempDir tmp: Path) {
        val config = TestConfigForNaming()
        val file = tmp.resolve("test.yaml")

        val nt = nt()
        nt.namingStrategy = NamingStrategy.PASCAL_CASE
        nt.save(file, config)

        val content = file.readText()
        println("Pascal Case Naming:")
        println(content)

        // Check naming strategy applied
        assertContains(content, "ServerName:")
        assertContains(content, "PortNumber:")
        assertContains(content, "EnableDebug:")
        assertContains(content, "MaxConnectionCount:")

        // Check explicit names preserved
        assertContains(content, "custom_field:") // from @SerializedName
        assertContains(content, "config_prop_name:") // from @ConfigProperty(name)
    }

    @Test
    fun test_camel_case_naming_default(@TempDir tmp: Path) {
        val config = TestConfigForNaming()
        val file = tmp.resolve("test.yaml")

        val nt = nt()
        // CAMEL_CASE is same as IDENTITY for field names
        nt.namingStrategy = NamingStrategy.CAMEL_CASE
        nt.save(file, config)

        val content = file.readText()
        println("Camel Case Naming (default):")
        println(content)

        // Check original names preserved
        assertContains(content, "serverName:")
        assertContains(content, "portNumber:")
        assertContains(content, "enableDebug:")
        assertContains(content, "maxConnectionCount:")

        // Check explicit names preserved
        assertContains(content, "custom_field:") // from @SerializedName
        assertContains(content, "config_prop_name:") // from @ConfigProperty(name)
    }

    @Test
    fun test_load_with_naming_strategy(@TempDir tmp: Path) {
        // Save with snake_case naming
        val originalConfig = TestConfigForNaming()
        originalConfig.serverName = "loaded-server"
        originalConfig.portNumber = 9090
        originalConfig.enableDebug = false

        val file = tmp.resolve("test.yaml")
        val nt = nt()
        nt.namingStrategy = NamingStrategy.SNAKE_CASE
        nt.save(file, originalConfig)

        // Load back with the same naming strategy
        val loadedConfig = nt.load(file, TestConfigForNaming::class.java)

        // Verify values are correctly loaded
        assertEquals("loaded-server", loadedConfig.serverName)
        assertEquals(9090, loadedConfig.portNumber)
        assertEquals(false, loadedConfig.enableDebug)
        assertEquals("100", loadedConfig.maxConnectionCount)
        assertEquals("should-keep-explicit-name", loadedConfig.fieldWithExplicitName)
        assertEquals("should-keep-config-prop-name", loadedConfig.fieldWithConfigProperty)
    }

    @Test
    fun test_header_and_naming_combined(@TempDir tmp: Path) {
        val config = TestConfigWithHeader()
        val file = tmp.resolve("test.yaml")

        val nt = nt()
        nt.namingStrategy = NamingStrategy.SNAKE_CASE
        nt.save(file, config)

        val content = file.readText()
        println("Combined Header + Snake Case:")
        println(content)

        // Check header is present
        assertContains(content, "# This is a test configuration file.")
        assertContains(content, "# Generated for testing purposes.")

        // Check naming strategy applied
        assertContains(content, "server_name:")
        assertContains(content, "port_number:")
        assertContains(content, "enable_debug:")

        // Check explicit names preserved
        assertContains(content, "custom_field:") // from @SerializedName
        assertContains(content, "another_custom:") // from @ConfigProperty(name)
    }
}