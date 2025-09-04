package re.neotamia.config.toml

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlIndentation
import com.akuleshov7.ktoml.TomlInputConfig
import com.akuleshov7.ktoml.TomlOutputConfig
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import re.neotamia.config.format.ConfigFormat
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.reflect.KClass

class TomlConfigFormat(val toml: Toml = Toml(
    inputConfig = TomlInputConfig(ignoreUnknownNames = true),
    outputConfig = TomlOutputConfig(indentation = TomlIndentation.TWO_SPACES)
)) : ConfigFormat(true, setOf("toml")) {
    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> read(path: Path, clazz: KClass<T>): T {
        val text = path.readText()
        return toml.decodeFromString(clazz.serializer(), text)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> write(path: Path, config: T, clazz: KClass<T>) {
        val text = toml.encodeToString(clazz.serializer(), config)
        path.toFile().writeText(text)
    }
}
