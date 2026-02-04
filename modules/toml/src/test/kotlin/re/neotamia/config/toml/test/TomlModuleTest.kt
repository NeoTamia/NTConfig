package re.neotamia.config.toml.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.toml.registerToml
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

class TomlModuleTest {
    @TempDir
    lateinit var tempDir: Path

    class SampleConfig {
        var name: String = "value"
    }

    @Test
    fun `register saves toml file`() {
        val ntConfig = NTConfig()
        ntConfig.registerToml()

        val path = tempDir.resolve("sample.toml")
        val saved = ntConfig.save(path, SampleConfig())
        saved.close()

        assertTrue(Files.exists(path))
        assertTrue(Files.readString(path).contains("name"))
    }
}
