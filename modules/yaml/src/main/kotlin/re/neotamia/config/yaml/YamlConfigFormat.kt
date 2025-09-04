package re.neotamia.config.yaml

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import re.neotamia.config.format.ConfigFormat
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.reflect.KClass

class YamlConfigFormat(val yaml: Yaml = Yaml(
    configuration = YamlConfiguration(sequenceBlockIndent = 2, strictMode = false)
)) : ConfigFormat(true, setOf("yaml", "yml")) {
    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> read(path: Path, clazz: KClass<T>): T {
        val text = path.readText()
        return yaml.decodeFromString(clazz.serializer(), text)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> write(path: Path, config: T, clazz: KClass<T>) {
        val text = yaml.encodeToString(clazz.serializer(), config)
        path.toFile().writeText(text)
    }
}