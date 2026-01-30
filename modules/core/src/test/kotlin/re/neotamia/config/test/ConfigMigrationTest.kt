package re.neotamia.config.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.annotation.ConfigVersion
import re.neotamia.config.migration.*
import re.neotamia.nightconfig.core.CommentedConfig
import re.neotamia.nightconfig.core.Config
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import re.neotamia.config.migration.ConfigVersion as MigrationVersion

class ConfigMigrationTest {
    @TempDir
    lateinit var tempDir: Path

    class TestConfig {
        @ConfigVersion(defaultVersion = "2")
        var version: Int = 2

        var name: String = "default"
        var value: Int = 10

        // Nullable to simulate field that might be missing in loaded config
        var newField: String? = null

        var deprecatedField: String? = null
    }

    @Test
    fun `test migration updates version and adds missing fields (MERGE_MISSING_ONLY)`() {
        val backupManager = BackupManager(tempDir.resolve("backups"))
        val migrationManager = ConfigMigrationManager(backupManager)
        val configPath = tempDir.resolve("config.json")

        // Create dummy config file so backup manager can back it up
        Files.createFile(configPath)

        // Simulate loaded config (V1 data)
        val loadedConfig = TestConfig()
        loadedConfig.version = 1
        loadedConfig.name = "userValue"
        loadedConfig.value = 99
        loadedConfig.newField = null
        loadedConfig.deprecatedField = "oldData"

        // Template (V2 defaults)
        val template = TestConfig()
        template.version = 2
        template.name = "default"
        template.value = 10
        template.newField = "newValue"
        template.deprecatedField = null

        val result = migrationManager.migrate(configPath, loadedConfig, template, MergeStrategy.MERGE_MISSING_ONLY)

        assertTrue(result.wasMigrated(), "Should be migrated")
        assertTrue(result.hasBackup(), "Should have backup")

        val migratedConfig = result.config
        assertEquals(2, migratedConfig.version, "Version should be updated to 2")
        assertEquals("userValue", migratedConfig.name, "Existing field should be preserved")
        assertEquals(99, migratedConfig.value, "Existing field should be preserved")
        assertEquals("newValue", migratedConfig.newField, "Missing field should be populated from template")
        assertEquals("oldData", migratedConfig.deprecatedField, "Deprecated field should be preserved in MERGE_MISSING_ONLY")
    }

    @Test
    fun `test migration with OVERRIDE`() {
        val migrationManager = ConfigMigrationManager(BackupManager(tempDir))
        val configPath = tempDir.resolve("config.json")

        // Create dummy config file so backup manager can back it up
        Files.createFile(configPath)

        val loadedConfig = TestConfig()
        loadedConfig.version = 1
        loadedConfig.name = "userValue"
        loadedConfig.value = 99

        val template = TestConfig()
        template.version = 2
        template.name = "default"
        template.value = 10
        template.newField = "newValue"

        val result = migrationManager.migrate(configPath, loadedConfig, template, MergeStrategy.OVERRIDE)

        assertTrue(result.wasMigrated())

        // Should be exactly the template
        val migratedConfig = result.config
        assertEquals(2, migratedConfig.version)
        assertEquals("default", migratedConfig.name)
        assertEquals(10, migratedConfig.value)
        assertEquals("newValue", migratedConfig.newField)
    }

    @Test
    fun `test migration with VERSION_ONLY`() {
        val migrationManager = ConfigMigrationManager(BackupManager(tempDir))
        val configPath = tempDir.resolve("config.json")

        // Create dummy config file so backup manager can back it up
        Files.createFile(configPath)

        val loadedConfig = TestConfig()
        loadedConfig.version = 1
        loadedConfig.name = "userValue"
        loadedConfig.value = 99
        loadedConfig.newField = null

        val template = TestConfig()
        template.version = 2
        template.name = "default"
        template.value = 10
        template.newField = "newValue"

        val result = migrationManager.migrate(configPath, loadedConfig, template, MergeStrategy.VERSION_ONLY)

        assertTrue(result.wasMigrated())

        val migratedConfig = result.config
        assertEquals(2, migratedConfig.version)
        assertEquals("userValue", migratedConfig.name)
        assertEquals(99, migratedConfig.value)
        assertNull(migratedConfig.newField) // Should NOT be updated
    }

    @Test
    fun `test no migration needed when versions match`() {
        val migrationManager = ConfigMigrationManager(BackupManager(tempDir))
        val configPath = tempDir.resolve("config.json")

        val loadedConfig = TestConfig()
        loadedConfig.version = 2

        val template = TestConfig()
        template.version = 2

        val result = migrationManager.migrate(configPath, loadedConfig, template, MergeStrategy.MERGE_MISSING_ONLY)

        assertFalse(result.wasMigrated())
        assertEquals(loadedConfig, result.config)
    }

    @Test
    fun `test raw config migration steps apply in order`() {
        val backupManager = BackupManager(tempDir.resolve("backups"))
        val migrationManager = ConfigMigrationManager(backupManager)
        val configPath = tempDir.resolve("config.yml")

        Files.writeString(configPath, "server: proxy\nversion: 1\n")

        val config = Config.inMemory()
        config.set<String>("version", "1")
        config.set<String>("server", "proxy")

        migrationManager.addConfigMigrationStep(object : ConfigMigrationStep {
            override fun fromVersion(): MigrationVersion = MigrationVersion("1")

            override fun toVersion(): MigrationVersion = MigrationVersion("2")

            override fun migrate(config: Config) {
                val server = config.get<String>("server")
                config.set<String>("server.id", server)
                config.remove<String>("server")
            }
        })

        migrationManager.addConfigMigrationStep(object : ConfigMigrationStep {
            override fun fromVersion(): MigrationVersion = MigrationVersion("2")

            override fun toVersion(): MigrationVersion = MigrationVersion("3")

            override fun migrate(config: Config) {
                config.set<String>("server.enabled", true)
            }
        })

        val result = migrationManager.migrateConfig(
            configPath,
            config,
            MigrationVersion("3"),
            "version",
            MigrationVersion("1")
        )

        assertTrue(result.wasMigrated())
        assertTrue(result.hasBackup())
        assertEquals("proxy", config.get<String>("server.id"))
        assertEquals(true, config.get<Boolean>("server.enabled"))
        assertEquals("3", config.get<String>("version"))
    }

    @Test
    fun `test commented config migration step is used`() {
        val migrationManager = ConfigMigrationManager(BackupManager(tempDir))
        val configPath = tempDir.resolve("config.yml")
        Files.writeString(configPath, "version: 1\n")

        val config = CommentedConfig.inMemory()
        config.set<String>("version", "1")

        val trackingStep = object : CommentedConfigMigrationStep {
            var usedCommented = false

            override fun fromVersion(): MigrationVersion = MigrationVersion("1")

            override fun toVersion(): MigrationVersion = MigrationVersion("2")

            override fun migrate(config: CommentedConfig) {
                usedCommented = true
                config.set<Boolean>("migrated", true)
            }
        }

        migrationManager.addConfigMigrationStep(trackingStep)

        val result = migrationManager.migrateConfig(
            configPath,
            config,
            MigrationVersion("2"),
            "version",
            MigrationVersion("1")
        )

        assertTrue(result.wasMigrated())
        assertTrue(trackingStep.usedCommented)
        assertEquals(true, config.get<Boolean>("migrated"))
    }
}
