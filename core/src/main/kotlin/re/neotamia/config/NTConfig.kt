package re.neotamia.config

import java.io.IOException
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.io.path.extension

class NTConfig {
    val registry = ConfigRegistry()

    @Throws(IOException::class, IllegalArgumentException::class)
    inline fun <reified T: Any> load(path: Path): T {
        return registry.getFormatFromExtension(path.extension)?.read(path, T::class) ?: throw IllegalArgumentException("No config format found for extension: ${path.extension}")
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    inline fun <reified T: Any> save(path: Path, value: T) {
        registry.getFormatFromExtension(path.extension)?.write(path, value, T::class) ?: throw IllegalArgumentException("No config format found for extension: ${path.extension}")
    }
}