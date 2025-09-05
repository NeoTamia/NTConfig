package re.neotamia.config.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.annotation.Comment
import re.neotamia.config.annotation.ConfigProperty
import re.neotamia.config.annotation.Exclude
import re.neotamia.config.json.JsonSerializer
import re.neotamia.config.toml.TomlSerializer
import re.neotamia.config.yaml.YamlSerializer
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommentAndExclusionTest {

    class TestConfig {
        @Comment("This is a commented field")
        var fieldWithComment: String = "value1"

        @ConfigProperty("This is a description field")
        var fieldWithDescription: String = "value2"

        @ConfigProperty(name = "custom_name", value = "Field with custom name and description")
        var fieldWithCustomName: String = "value3"

        var normalField: String = "value4"

        @Exclude
        var excludedField: String = "excluded1"

        @ConfigProperty(exclude = true)
        var configExcludedField: String = "excluded2"

        @ConfigProperty(exclude = true, value = "This should be excluded despite having description")
        var excludedWithDescription: String = "excluded3"
    }

    private fun nt(): NTConfig {
        val nt = NTConfig()
        nt.registerSerializer(YamlSerializer())
        nt.registerSerializer(JsonSerializer())
        nt.registerSerializer(TomlSerializer())
        return nt
    }

    @Test
    fun yaml_comments_and_exclusions(@TempDir tmp: Path) {
        val config = TestConfig()
        val file = tmp.resolve("test.yaml")

        nt().save(file, config)
        val content = file.readText()

        println("YAML Output:")
        println(content)

        // Check comments are present
        assertContains(content, "# This is a commented field")
        assertContains(content, "# This is a description field")
        assertContains(content, "# Field with custom name and description")

        // Check field values are present
        assertContains(content, "fieldWithComment: \"value1\"")
        assertContains(content, "fieldWithDescription: \"value2\"")
        assertContains(content, "custom_name: \"value3\"") // custom name
        assertContains(content, "normalField: \"value4\"")

        // Check excluded fields are not present
        assertFalse(content.contains("excludedField"))
        assertFalse(content.contains("configExcludedField"))
        assertFalse(content.contains("excludedWithDescription"))
        assertFalse(content.contains("excluded1"))
        assertFalse(content.contains("excluded2"))
        assertFalse(content.contains("excluded3"))
    }

    @Test
    fun toml_comments_and_exclusions(@TempDir tmp: Path) {
        val config = TestConfig()
        val file = tmp.resolve("test.toml")

        nt().save(file, config)
        val content = file.readText()

        println("TOML Output:")
        println(content)

        // Check comments are present
        assertContains(content, "# This is a commented field")
        assertContains(content, "# This is a description field")
        assertContains(content, "# Field with custom name and description")

        // Check field values are present
        assertContains(content, "fieldWithComment = 'value1'")
        assertContains(content, "fieldWithDescription = 'value2'")
        assertContains(content, "custom_name = 'value3'") // custom name
        assertContains(content, "normalField = 'value4'")

        // Check excluded fields are not present
        assertFalse(content.contains("excludedField"))
        assertFalse(content.contains("configExcludedField"))
        assertFalse(content.contains("excludedWithDescription"))
        assertFalse(content.contains("excluded1"))
        assertFalse(content.contains("excluded2"))
        assertFalse(content.contains("excluded3"))
    }

    @Test
    fun json_no_comments_but_exclusions(@TempDir tmp: Path) {
        val config = TestConfig()
        val file = tmp.resolve("test.json")

        nt().save(file, config)
        val content = file.readText()

        println("JSON Output:")
        println(content)

        // JSON should not have comments
        assertFalse(content.contains("#"))

        // Check field values are present
        assertContains(content, "\"fieldWithComment\": \"value1\"")
        assertContains(content, "\"fieldWithDescription\": \"value2\"")
        assertContains(content, "\"custom_name\": \"value3\"") // custom name
        assertContains(content, "\"normalField\": \"value4\"")

        // Check excluded fields are not present
        assertFalse(content.contains("excludedField"))
        assertFalse(content.contains("configExcludedField"))
        assertFalse(content.contains("excludedWithDescription"))
        assertFalse(content.contains("excluded1"))
        assertFalse(content.contains("excluded2"))
        assertFalse(content.contains("excluded3"))
    }

    @Test
    fun load_config_respects_exclusions(@TempDir tmp: Path) {
        // Create a config with excluded fields included in the file
        val file = tmp.resolve("test.yaml")
        file.writeText(
            """
            fieldWithComment: loadedValue1
            fieldWithDescription: loadedValue2
            custom_name: loadedValue3
            normalField: loadedValue4
            excludedField: shouldBeIgnored1
            configExcludedField: shouldBeIgnored2
            excludedWithDescription: shouldBeIgnored3
        """.trimIndent()
        )

        val loadedConfig = nt().load(file, TestConfig::class.java)

        // Check that regular fields are loaded
        assertTrue { loadedConfig.fieldWithComment == "loadedValue1" }
        assertTrue { loadedConfig.fieldWithDescription == "loadedValue2" }
        assertTrue { loadedConfig.fieldWithCustomName == "loadedValue3" }
        assertTrue { loadedConfig.normalField == "loadedValue4" }

        // Check that excluded fields keep their default values (not loaded from file)
        assertTrue { loadedConfig.excludedField == "excluded1" }
        assertTrue { loadedConfig.configExcludedField == "excluded2" }
        assertTrue { loadedConfig.excludedWithDescription == "excluded3" }
    }
}