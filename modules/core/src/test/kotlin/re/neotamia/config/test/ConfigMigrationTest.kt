package re.neotamia.config.test

// @formatter:off
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.annotation.ConfigVersion
import re.neotamia.config.migration.BackupManager
import re.neotamia.config.migration.ConfigMigrationManager
import re.neotamia.config.migration.MergeStrategy
import java.nio.file.Files
import java.nio.file.Path

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
}
