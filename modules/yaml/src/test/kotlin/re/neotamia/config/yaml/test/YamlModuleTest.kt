package re.neotamia.config.yaml.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.yaml.registerYaml
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

class YamlModuleTest {
    @TempDir
    lateinit var tempDir: Path

    class SampleConfig {
        var name: String = "value"
    }

    @Test
    fun `register saves yaml file`() {
        val ntConfig = NTConfig()
        ntConfig.registerYaml()

        val path = tempDir.resolve("sample.yaml")
        val saved = ntConfig.save(path, SampleConfig())
        saved.close()

        assertTrue(Files.exists(path))
        assertTrue(Files.readString(path).contains("name"))
    }
}
