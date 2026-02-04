package re.neotamia.config.json.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.format.FormatModules
import re.neotamia.config.json.registerJson
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

class JsonModuleTest {
    @TempDir
    lateinit var tempDir: Path

    class SampleConfig {
        var name: String = "value"
    }

    @Test
    fun `register saves json file`() {
        val ntConfig = NTConfig()
        ntConfig.registerJson()

        val path = tempDir.resolve("sample.json")
        val saved = ntConfig.save(path, SampleConfig())
        saved.close()

        assertTrue(Files.exists(path))
        assertTrue(Files.readString(path).contains("name"))
    }

    @Test
    fun `service loader registers json format`() {
        val ntConfig = NTConfig()
        FormatModules.registerAvailable(ntConfig)

        val path = tempDir.resolve("auto.json")
        val saved = ntConfig.save(path, SampleConfig())
        saved.close()

        assertTrue(Files.exists(path))
        assertTrue(Files.readString(path).contains("name"))
    }
}
