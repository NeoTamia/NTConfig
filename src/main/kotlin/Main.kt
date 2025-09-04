package re.neotamia.config.main;

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import com.akuleshov7.ktoml.annotations.TomlMultiline
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlNamingStrategy.Builtins.KebabCase
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Serializable
class Config {
    val name: String = "Config"
    val version: Int = 1
    val enabled: Boolean = true
    val decimals: Float = 0.5f
    val double: Double = 0.123456789
    val items: List<String> = listOf("item1", "item2", "item3")
    val settings: Map<String, String> = mapOf("key1" to "value1", "key2" to "value2")
    val nested: NestedConfig = NestedConfig()
    val multiline: String = """
        This is a multiline
        string example.
        It preserves line breaks.
    """.trimIndent()

    override fun toString(): String {
        return "Config(name='$name', version=$version, enabled=$enabled, decimals=$decimals, double=$double, items=$items, settings=$settings, nested=$nested, multiline='$multiline')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Config

        if (version != other.version) return false
        if (enabled != other.enabled) return false
        if (decimals != other.decimals) return false
        if (double != other.double) return false
        if (name != other.name) return false
        if (items != other.items) return false
        if (settings != other.settings) return false
        if (nested != other.nested) return false
        if (multiline != other.multiline) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + enabled.hashCode()
        result = 31 * result + decimals.hashCode()
        result = 31 * result + double.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + items.hashCode()
        result = 31 * result + settings.hashCode()
        result = 31 * result + nested.hashCode()
        result = 31 * result + multiline.hashCode()
        return result
    }
}

@Serializable
class NestedConfig {
    val description: String = "Nested Config"
    val count: Int = 10
    val nested = InnerNestedConfig()

    override fun toString(): String {
        return "NestedConfig(description='$description', count=$count, nested=$nested)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NestedConfig

        if (count != other.count) return false
        if (description != other.description) return false
        if (nested != other.nested) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + description.hashCode()
        result = 31 * result + nested.hashCode()
        return result
    }
}

@Serializable
class InnerNestedConfig {
    val flag: Boolean = false
    val ratio: Double = 3.14

    override fun toString(): String {
        return "InnerNestedConfig(flag=$flag, ratio=$ratio)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InnerNestedConfig

        if (flag != other.flag) return false
        if (ratio != other.ratio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = flag.hashCode()
        result = 31 * result + ratio.hashCode()
        return result
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val json = Json { encodeDefaults = true; prettyPrint = true; ignoreUnknownKeys = true; allowComments = true; prettyPrintIndent = "  " }
    val toml = Toml(inputConfig = TomlInputConfig(ignoreUnknownNames = true), outputConfig = TomlOutputConfig(indentation = TomlIndentation.TWO_SPACES))
    val yaml = Yaml(configuration = YamlConfiguration(sequenceBlockIndent = 2, yamlNamingStrategy = KebabCase, strictMode = false))

    val config = Config()
    val jsonString = json.encodeToString(Config.serializer(), config)
    val tomlString = toml.encodeToString(Config.serializer(), config)
    val yamlString = yaml.encodeToString(Config.serializer(), config)

    Files.write(Path.of("config.json"), jsonString.toByteArray())
    Files.write(Path.of("config.toml"), tomlString.toByteArray())
    Files.write(Path.of("config.yaml"), yamlString.toByteArray())

    val jsonConfig = json.decodeFromString<Config>(Files.readString(Path.of("config.json")))
    val tomlConfig = toml.decodeFromString<Config>(Files.readString(Path.of("config.toml")))
    val yamlConfig = yaml.decodeFromString<Config>(Files.readString(Path.of("config.yaml")))
    println("JSON Config == Config: ${jsonConfig == config}")
    println("TOML Config == Config: ${tomlConfig == config}")
    println("YAML Config == Config: ${yamlConfig == config}")
}
