package re.neotamia.config.test

import re.neotamia.config.NTConfig
import re.neotamia.config.saveable.Saveable
import re.neotamia.config.saveable.SaveableCommented
import re.neotamia.nightconfig.core.file.CommentedFileConfig
import re.neotamia.nightconfig.core.file.FileConfig
import re.neotamia.nightconfig.yaml.YamlFormat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveableTest {
    private lateinit var tempDir: Path
    private lateinit var ntConfig: NTConfig

    @BeforeTest
    fun setup() {
        tempDir = Files.createTempDirectory("ntconfig-test")
        ntConfig = NTConfig()
        ntConfig.registerFormat(YamlFormat.defaultInstance(), "yaml", "yml")
    }

    @AfterTest
    fun tearDown() {
        Files.walk(tempDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
    }

    class ManualConfig : Saveable {
        var value: String = "default"
        var saved: Boolean = false
        var loaded: Boolean = false

        override fun save(fileConfig: FileConfig) {
            fileConfig.set<String>("manual-value", value)
            saved = true
        }

        override fun load(fileConfig: FileConfig) {
            value = fileConfig.getOrElse("manual-value", "failed")
            loaded = true
        }
    }

    class ManualCommentedConfig : SaveableCommented {
        var value: String = "default"
        var saved: Boolean = false
        var loaded: Boolean = false

        override fun save(fileConfig: CommentedFileConfig) {
            fileConfig.set<String>("manual-value", value)
            fileConfig.setComment("manual-value", "This is a comment")
            saved = true
        }

        override fun load(fileConfig: CommentedFileConfig) {
            value = fileConfig.getOrElse("manual-value", "failed")
            loaded = true
        }
    }

    @Test
    fun testSaveable() {
        val config = ManualConfig()
        config.value = "test-saveable"
        val path = tempDir.resolve("test.yaml")

        ntConfig.save(path, config)
        assertTrue(config.saved)

        val newConfig = ManualConfig()
        ntConfig.load(path, newConfig)
        assertTrue(newConfig.loaded)
        assertEquals("test-saveable", newConfig.value)
    }

    @Test
    fun testSaveableCommented() {
        val config = ManualCommentedConfig()
        config.value = "test-commented"
        val path = tempDir.resolve("test-commented.yaml")

        ntConfig.save(path, config)
        assertTrue(config.saved)

        // Verify it's actually a commented file
        val content = Files.readString(path)
        assertTrue(content.contains("# This is a comment"))

        val newConfig = ManualCommentedConfig()
        ntConfig.load(path, newConfig)
        assertTrue(newConfig.loaded)
        assertEquals("test-commented", newConfig.value)
    }
}
