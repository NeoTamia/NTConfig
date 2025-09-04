package re.neotamia.config.format

import org.jetbrains.annotations.NotNull
import java.nio.file.Path
import kotlin.jvm.Throws
import kotlin.reflect.KClass

/**
 * Abstract class representing a configuration file format. It provides methods for reading and
 * writing configuration data while allowing support for custom formats.
 *
 * @property supportComments Indicates if the format supports comments.
 * @property supportedExtensions A set of file extensions that the format supports. Need to be lowercase without a dot.
 */
abstract class ConfigFormat(val supportComments: Boolean, val supportedExtensions: Set<String>) {
    @Throws(Exception::class)
    abstract fun <T: Any> read(path: Path, clazz: KClass<T>): @NotNull T

    @Throws(Exception::class)
    abstract fun <T: Any> write(path: Path, config: T, clazz: KClass<T>)
}