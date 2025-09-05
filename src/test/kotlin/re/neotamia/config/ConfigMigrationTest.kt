package re.neotamia.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.annotation.Comment
import re.neotamia.config.annotation.ConfigHeader
import re.neotamia.config.annotation.ConfigProperty
import re.neotamia.config.annotation.ConfigVersion
import re.neotamia.config.json.JsonSerializer
import re.neotamia.config.migration.MergeStrategy
import re.neotamia.config.migration.MigrationHook
import re.neotamia.config.toml.TomlSerializer
import re.neotamia.config.yaml.YamlSerializer
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigMigrationTest {

    @ConfigHeader("Test Configuration v1")
    class TestConfigV1 {
        @ConfigVersion(defaultVersion = "1")
        var version: String = "1"

        @Comment("Server name")
        var serverName: String = "default-server"

        @Comment("Server port")
        var port: Int = 8080

        var enableLogging: Boolean = true
    }

    @ConfigHeader("Test Configuration v2")
    class TestConfigV2 {
        @ConfigVersion(defaultVersion = "2")
        var version: String = "2"

        @Comment("Server name")
        var serverName: String = "default-server"

        @Comment("Server port")
        var port: Int = 8080

        var enableLogging: Boolean = true

        @Comment("New field in v2")
        var maxConnections: Int = 100

        @ConfigProperty("Debug mode setting")
        var debugMode: Boolean = false
    }

    class TestConfigIntegerVersion {
        @ConfigVersion(defaultVersion = "1")
        var version: Int = 1

        var setting: String = "test"
    }

    class TestConfigSemanticVersion {
        @ConfigVersion(defaultVersion = "1.0.0")
        var version: String = "1.0.0"

        var feature: String = "basic"
    }

    private fun nt(): NTConfig {
        val nt = NTConfig()
        nt.registerSerializer(YamlSerializer())
        nt.registerSerializer(JsonSerializer())
        nt.registerSerializer(TomlSerializer())
        return nt
    }

    @Test
    fun test_new_config_creation(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")
        val currentTemplate = TestConfigV2()

        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate)

        // Should create new config
        assertTrue(configFile.exists())
        assertFalse(result.wasMigrated())
        assertEquals("2", result.config().version)
        assertEquals("default-server", result.config().serverName)
        assertEquals(8080, result.config().port)
        assertEquals(100, result.config().maxConnections)
        assertEquals(false, result.config().debugMode)
    }

    @Test
    fun test_no_migration_needed_same_version(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        // Create existing v2 config
        configFile.writeText(
            """
            version: "2"
            serverName: "my-server"
            port: 9090
            enableLogging: false
            maxConnections: 200
            debugMode: true
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate)

        // Should not migrate
        assertFalse(result.wasMigrated())
        assertEquals("2", result.config().version)
        assertEquals("my-server", result.config().serverName)
        assertEquals(9090, result.config().port)
        assertEquals(false, result.config().enableLogging)
        assertEquals(200, result.config().maxConnections)
        assertEquals(true, result.config().debugMode)
    }

    @Test
    fun test_migration_v1_to_v2_merge_missing_only(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        // Create v1 config
        configFile.writeText(
            """
            version: "1"
            serverName: "old-server"
            port: 3000
            enableLogging: false
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate, MergeStrategy.MERGE_MISSING_ONLY)

        // Should migrate and add missing fields only
        assertTrue(result.wasMigrated())
        assertEquals("1", result.oldVersion()?.version)
        assertEquals("2", result.newVersion()?.version)
        assertEquals("2", result.config().version)

        // Old values should be preserved
        assertEquals("old-server", result.config().serverName)
        assertEquals(3000, result.config().port)
        assertEquals(false, result.config().enableLogging)

        // New fields should have default values
        assertEquals(100, result.config().maxConnections)
        assertEquals(false, result.config().debugMode)

        // Should have backup
        assertTrue(result.hasBackup())
        assertTrue(result.backupPath()!!.exists())
    }

    @Test
    fun test_migration_override_strategy(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        // Create v1 config with custom values
        configFile.writeText(
            """
            version: "1"
            serverName: "custom-server"
            port: 5000
            enableLogging: false
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        currentTemplate.serverName = "template-server"
        currentTemplate.port = 7000

        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate, MergeStrategy.OVERRIDE)

        // Should completely replace with template
        assertTrue(result.wasMigrated())
        assertEquals("2", result.config().version)
        assertEquals("template-server", result.config().serverName)
        assertEquals(7000, result.config().port)
        assertEquals(true, result.config().enableLogging) // template default
        assertEquals(100, result.config().maxConnections)
        assertEquals(false, result.config().debugMode)
    }

    @Test
    fun test_migration_version_only_strategy(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        // Create v1 config
        configFile.writeText(
            """
            version: "1"
            serverName: "keep-this-server"
            port: 9999
            enableLogging: false
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate, MergeStrategy.VERSION_ONLY)

        // Should only update version, keep everything else
        assertTrue(result.wasMigrated())
        assertEquals("2", result.config().version)
        assertEquals("keep-this-server", result.config().serverName)
        assertEquals(9999, result.config().port)
        assertEquals(false, result.config().enableLogging)

        // New fields should not be added in VERSION_ONLY mode
        // Note: In this implementation, missing fields won't be accessible
        // The loaded config is still v1 structure with updated version
    }

    @Test
    fun test_integer_version_migration(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        configFile.writeText(
            """
            version: 1
            setting: "old-value"
        """.trimIndent()
        )

        val currentTemplate = TestConfigIntegerVersion()
        currentTemplate.version = 2
        currentTemplate.setting = "new-default"

        val result = nt().loadWithMigration(configFile, TestConfigIntegerVersion::class.java, currentTemplate, MergeStrategy.MERGE_MISSING_ONLY)

        assertTrue(result.wasMigrated())
        assertEquals(1, result.oldVersion()?.major)
        assertEquals(2, result.newVersion()?.major)
        assertEquals(2, result.config().version)
        assertEquals("old-value", result.config().setting) // Preserved
    }

    @Test
    fun test_semantic_version_migration(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        configFile.writeText(
            """
            version: "1.0.0"
            feature: "old-feature"
        """.trimIndent()
        )

        val currentTemplate = TestConfigSemanticVersion()
        currentTemplate.version = "1.2.0"
        currentTemplate.feature = "new-feature"

        val result = nt().loadWithMigration(configFile, TestConfigSemanticVersion::class.java, currentTemplate, MergeStrategy.MERGE_MISSING_ONLY)

        assertTrue(result.wasMigrated())
        assertEquals("1.0.0", result.oldVersion()?.version)
        assertEquals("1.2.0", result.newVersion()?.version)
        assertEquals("1.2.0", result.config().version)
        assertEquals("old-feature", result.config().feature) // Preserved
    }

    @Test
    fun test_migration_hooks(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        configFile.writeText(
            """
            version: "1"
            serverName: "test"
            port: 8080
            enableLogging: true
        """.trimIndent()
        )

        val events = mutableListOf<String>()
        val hook = object : MigrationHook {
            override fun beforeMigration(
                configPath: Path,
                oldVersion: re.neotamia.config.migration.ConfigVersion?,
                newVersion: re.neotamia.config.migration.ConfigVersion?,
                strategy: re.neotamia.config.migration.MergeStrategy?
            ) {
                events.add("beforeMigration: ${oldVersion?.version} -> ${newVersion?.version}")
            }

            override fun afterBackup(
                configPath: Path,
                backupPath: Path?,
                oldVersion: re.neotamia.config.migration.ConfigVersion?,
                newVersion: re.neotamia.config.migration.ConfigVersion?
            ) {
                events.add("afterBackup: ${backupPath != null}")
            }

            override fun afterMigration(
                configPath: Path,
                backupPath: Path?,
                oldVersion: re.neotamia.config.migration.ConfigVersion?,
                newVersion: re.neotamia.config.migration.ConfigVersion?,
                strategy: re.neotamia.config.migration.MergeStrategy?
            ) {
                events.add("afterMigration: success")
            }
        }

        val nt = nt()
        nt.addMigrationHook(hook)

        val currentTemplate = TestConfigV2()
        val result = nt.loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate)

        assertTrue(result.wasMigrated())
        assertEquals(3, events.size)
        assertEquals("beforeMigration: 1 -> 2", events[0])
        assertEquals("afterBackup: true", events[1])
        assertEquals("afterMigration: success", events[2])
    }

    @Test
    fun test_load_and_update_always_saves(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        configFile.writeText(
            """
            version: "2"
            serverName: "existing"
            port: 8080
            enableLogging: true
            maxConnections: 100
            debugMode: false
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        val result = nt().loadAndUpdate(configFile, TestConfigV2::class.java, currentTemplate)

        // Even though no migration was needed, file should be updated with current format
        assertFalse(result.wasMigrated())
        assertTrue(configFile.exists())

        val content = configFile.readText()
        // Should have comments and header from template
        assertContains(content, "# Test Configuration v2")
        assertContains(content, "# Server name")
        assertContains(content, "# Server port")
    }

    @Test
    fun test_backup_creation(@TempDir tmp: Path) {
        val configFile = tmp.resolve("config.yaml")

        configFile.writeText(
            """
            version: "1"
            serverName: "backup-test"
            port: 1234
            enableLogging: true
        """.trimIndent()
        )

        val currentTemplate = TestConfigV2()
        val result = nt().loadWithMigration(configFile, TestConfigV2::class.java, currentTemplate)

        assertTrue(result.wasMigrated())
        assertTrue(result.hasBackup())

        val backupContent = result.backupPath()!!.readText()
        assertContains(backupContent, "version: \"1\"")
        assertContains(backupContent, "serverName: \"backup-test\"")
        assertContains(backupContent, "port: 1234")

        // Backup filename should contain version and timestamp
        val backupFileName = result.backupPath()!!.fileName.toString()
        assertTrue(backupFileName.contains("_v1_"))
        assertTrue(backupFileName.contains("_backup_"))
        assertTrue(backupFileName.endsWith(".yaml"))
    }
}