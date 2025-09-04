package re.neotamia.config.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import re.neotamia.config.format.ConfigFormat
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.reflect.KClass

class JsonConfigFormat
@OptIn(ExperimentalSerializationApi::class)
constructor(
    val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrintIndent = "  "
    }
) : ConfigFormat(false, setOf("json")) {
    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> read(path: Path, clazz: KClass<T>): T {
        val text = path.readText()
        return json.decodeFromString(clazz.serializer(), text)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> write(path: Path, config: T, clazz: KClass<T>) {
        val text = json.encodeToString(clazz.serializer(), config)
        path.toFile().writeText(text)
    }

}
