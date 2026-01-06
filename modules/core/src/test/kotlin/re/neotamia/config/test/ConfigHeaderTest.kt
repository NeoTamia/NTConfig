package re.neotamia.config.test

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.annotation.ConfigHeader
import re.neotamia.nightconfig.yaml.YamlFormat
import java.nio.file.Path
import kotlin.io.path.readText

class ConfigHeaderTest {
    @TempDir
    lateinit var tempDir: Path

    @ConfigHeader("This is a test header")
    class TestConfig {
        var name: String = "test"
    }

    @Test
    fun `test config header is saved`() {
        val ntconfig = NTConfig()
        ntconfig.registerFormat(YamlFormat.defaultInstance(), "yaml", "yml")

        val configPath = tempDir.resolve("config.yml")
        val config = TestConfig()

        ntconfig.save(configPath, config)

        val content = configPath.readText()
        assertTrue(content.contains("This is a test header"), "File content should contain the header")
    }
}
