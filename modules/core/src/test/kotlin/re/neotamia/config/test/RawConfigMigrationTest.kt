package re.neotamia.config.test

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import re.neotamia.config.NTConfig
import re.neotamia.config.annotation.ConfigVersion
import re.neotamia.config.backup.BackupManager
import re.neotamia.config.migration.core.ConfigMigrationHelpers
import re.neotamia.config.migration.core.ConfigMigrationManager
import re.neotamia.config.migration.core.MergeStrategy
import re.neotamia.config.migration.core.MissingStepPolicy
import re.neotamia.config.migration.step.ConfigMigrationStep
import re.neotamia.config.migration.step.ICommentedConfigMigrationStep
import re.neotamia.config.migration.step.IConfigMigrationStep
import re.neotamia.config.migration.version.MigrationVersion
import re.neotamia.nightconfig.core.CommentedConfig
import re.neotamia.nightconfig.core.Config
import re.neotamia.nightconfig.json.JsonFormat
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RawConfigMigrationTest {
    @TempDir
    lateinit var tempDir: Path

    private fun newNtConfig(): NTConfig {
        val ntConfig = NTConfig()
        ntConfig.registerFormat(JsonFormat.fancyInstance(), "json")
        return ntConfig
    }

    class ServerConfig {
        @ConfigVersion(defaultVersion = "2")
        var version: Int = 2

        var server: Server = Server()
    }

    class Server {
        var id: String = "default"
        var port: Int = 25565
    }

    class ServerWrapStep : IConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("1")

        override fun toVersion(): MigrationVersion = MigrationVersion("2")

        override fun migrate(config: Config) {
            ConfigMigrationHelpers.wrapValue(config, "server", "id")
        }
    }

    class MissingFieldConfig {
        @ConfigVersion(defaultVersion = "1")
        var version: Int = 1

        var name: String = "default"
        var extra: String = "extra"
    }

    class RenameConfig {
        @ConfigVersion(defaultVersion = "2")
        var version: Int = 2

        var newName: String = "default"
    }

    class RenameStep : IConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("1")

        override fun toVersion(): MigrationVersion = MigrationVersion("2")

        override fun migrate(config: Config) {
            ConfigMigrationHelpers.rename(config, "old-name", "new-name")
        }
    }

    class ChainConfig {
        @ConfigVersion(defaultVersion = "3")
        var version: Int = 3

        var value: Int = 0
        var flag: Boolean = false
    }

    class StepOne : IConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("1")

        override fun toVersion(): MigrationVersion = MigrationVersion("2")

        override fun migrate(config: Config) {
            config.set<Int>("value", 42)
        }
    }

    class StepTwo : IConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("2")

        override fun toVersion(): MigrationVersion = MigrationVersion("3")

        override fun migrate(config: Config) {
            config.set<Boolean>("flag", true)
        }
    }

    class OverrideConfig {
        @ConfigVersion(defaultVersion = "1")
        var version: Int = 1

        var name: String = "default"
        var extra: String = "extra"
    }

    class NoStepVersionConfig {
        @ConfigVersion(defaultVersion = "2")
        var version: Int = 2

        var name: String = "default"
    }

    class PartialStepConfig {
        @ConfigVersion(defaultVersion = "3")
        var version: Int = 3

        var name: String = "default"
    }

    class CommentedConfigData {
        @ConfigVersion(defaultVersion = "2")
        var version: Int = 2

        var name: String = "default"
    }

    class StepOneOnly : IConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("1")

        override fun toVersion(): MigrationVersion = MigrationVersion("2")

        override fun migrate(config: Config) {
            config.set<String>("name", "migrated")
            config.set<Int>("version", 2)
        }
    }

    class CommentStep : ICommentedConfigMigrationStep {
        override fun fromVersion(): MigrationVersion = MigrationVersion("1")

        override fun toVersion(): MigrationVersion = MigrationVersion("2")

        override fun migrate(config: CommentedConfig) {
            config.set<String>("name", "commented")
            config.setComment("name", "Updated name")
        }
    }

    @Test
    fun `migrateAndLoad wraps scalar values and merges defaults`() {
        val ntConfig = newNtConfig()
        ntConfig.registerMigrationSteps(ServerConfig::class.java, ServerWrapStep())

        val path = tempDir.resolve("server.json")
        Files.writeString(path, """{"version":1,"server":"monserver"}""")

        val result = ntConfig.migrateAndLoad(path, ServerConfig::class.java, ServerConfig())

        assertTrue(result.wasMigrated())
        assertEquals(2, result.config.version)
        assertEquals("monserver", result.config.server.id)
        assertEquals(25565, result.config.server.port)
    }

    @Test
    fun `migrateAndLoad fills missing fields without steps`() {
        val ntConfig = newNtConfig()

        val path = tempDir.resolve("missing.json")
        Files.writeString(path, """{"version":1,"name":"custom"}""")

        val result = ntConfig.migrateAndLoad(path, MissingFieldConfig::class.java, MissingFieldConfig())

        assertEquals(1, result.config.version)
        assertEquals("custom", result.config.name)
        assertEquals("extra", result.config.extra)
    }

    @Test
    fun `migrateAndLoad applies rename steps`() {
        val ntConfig = newNtConfig()
        ntConfig.registerMigrationSteps(RenameConfig::class.java, RenameStep())

        val path = tempDir.resolve("rename.json")
        Files.writeString(path, """{"version":1,"old-name":"legacy"}""")

        val result = ntConfig.migrateAndLoad(path, RenameConfig::class.java, RenameConfig())

        assertTrue(result.wasMigrated())
        assertEquals(2, result.config.version)
        assertEquals("legacy", result.config.newName)
    }

    @Test
    fun `migrateAndLoad applies chained steps and updates version`() {
        val ntConfig = newNtConfig()
        ntConfig.registerMigrationSteps(ChainConfig::class.java, StepOne(), StepTwo())

        val path = tempDir.resolve("chain.json")
        Files.writeString(path, """{"version":1}""")

        val result = ntConfig.migrateAndLoad(path, ChainConfig::class.java, ChainConfig())

        assertTrue(result.wasMigrated())
        assertEquals(3, result.config.version)
        assertEquals(42, result.config.value)
        assertTrue(result.config.flag)
    }

    @Test
    fun `migrateAndLoad with OVERRIDE uses template values`() {
        val ntConfig = newNtConfig()

        val path = tempDir.resolve("override.json")
        Files.writeString(path, """{"version":1,"name":"custom"}""")

        val result = ntConfig.migrateAndLoad(path, OverrideConfig::class.java, OverrideConfig(), MergeStrategy.OVERRIDE)

        assertEquals("default", result.config.name)
        assertEquals("extra", result.config.extra)
    }

    @Test
    fun `migrateAndLoad with MERGE_MISSING_ONLY preserves custom values`() {
        val ntConfig = newNtConfig()

        val path = tempDir.resolve("merge-missing.json")
        Files.writeString(path, """{"version":1,"name":"custom"}""")

        val result = ntConfig.migrateAndLoad(path, OverrideConfig::class.java, OverrideConfig(), MergeStrategy.MERGE_MISSING_ONLY)

        assertEquals("custom", result.config.name)
        assertEquals("extra", result.config.extra)
        assertTrue(Files.readString(path).contains("extra"))
    }

    @Test
    fun `migrateAndLoad with VERSION_ONLY does not save missing fields`() {
        val ntConfig = newNtConfig()

        val path = tempDir.resolve("version-only.json")
        Files.writeString(path, """{"version":1,"name":"custom"}""")

        val result = ntConfig.migrateAndLoad(path, OverrideConfig::class.java, OverrideConfig(), MergeStrategy.VERSION_ONLY)

        assertEquals("custom", result.config.name)
        assertEquals("extra", result.config.extra)
        assertFalse(Files.readString(path).contains("extra"))
    }

    @Test
    fun `migrateAndLoad updates version when no steps are registered`() {
        val ntConfig = newNtConfig()

        val path = tempDir.resolve("no-steps.json")
        Files.writeString(path, """{"version":1,"name":"legacy"}""")

        val result = ntConfig.migrateAndLoad(path, NoStepVersionConfig::class.java, NoStepVersionConfig())

        assertTrue(result.wasMigrated())
        assertEquals(2, result.config.version)
        assertEquals("legacy", result.config.name)
    }

    @Test
    fun `missing step policy FAIL throws`() {
        val ntConfig = newNtConfig()
        ntConfig.registerMigrationSteps(PartialStepConfig::class.java, StepOneOnly())
        ntConfig.getMigrationManager().setMissingStepPolicy(MissingStepPolicy.FAIL)

        val path = tempDir.resolve("missing-step-fail.json")
        Files.writeString(path, """{"version":1,"name":"legacy"}""")

        assertFailsWith<RuntimeException> {
            ntConfig.migrateAndLoad(path, PartialStepConfig::class.java, PartialStepConfig())
        }
    }

    @Test
    fun `missing step policy SKIP keeps partial migration`() {
        val ntConfig = newNtConfig()
        ntConfig.registerMigrationSteps(PartialStepConfig::class.java, StepOneOnly())
        ntConfig.getMigrationManager().setMissingStepPolicy(MissingStepPolicy.SKIP)

        val path = tempDir.resolve("missing-step-skip.json")
        Files.writeString(path, """{"version":1,"name":"legacy"}""")

        val result = ntConfig.migrateAndLoad(path, PartialStepConfig::class.java, PartialStepConfig())

        assertEquals(2, result.config.version)
        assertEquals("migrated", result.config.name)
    }

    @Test
    fun `helpers move and copy handle overwrite`() {
        val config = Config.inMemory()
        config.set<String>("old", "value")
        config.set<String>("target", "existing")

        val moved = ConfigMigrationHelpers.move(config, "old", "target", false)
        assertFalse(moved)
        assertEquals("value", config.get<String>("old"))

        val copied = ConfigMigrationHelpers.copy(config, "old", "new", false)
        assertTrue(copied)
        assertEquals("value", config.get<String>("new"))
        assertEquals("value", config.get<String>("old"))
    }

    @Test
    fun `wrapValue skips when already an object`() {
        val config = Config.inMemory()
        val nested = Config.inMemory()
        nested.set<String>("id", "already")
        config.set<Config>("server", nested)

        val wrapped = ConfigMigrationHelpers.wrapValue(config, "server", "id")
        assertFalse(wrapped)
        val server = requireNotNull(config.get<Config>("server"))
        assertEquals("already", server.get<String>("id"))
    }

    @Test
    fun `commented migration step updates comments`() {
        val manager = ConfigMigrationManager(BackupManager(tempDir.resolve("backups")))
        manager.registerMigrationSteps(CommentedConfigData::class.java, CommentStep())

        val rawConfig = CommentedConfig.inMemory()
        rawConfig.set<Int>("version", 1)
        rawConfig.set<String>("name", "legacy")

        val result = manager.migrateRaw(
            tempDir.resolve("commented.toml"),
            rawConfig,
            CommentedConfigData::class.java,
            CommentedConfigData(),
            MergeStrategy.MERGE_MISSING_ONLY,
            null
        )

        assertTrue(result.wasMigrated())
        assertEquals("commented", rawConfig.get<String>("name"))
        assertEquals("Updated name", rawConfig.getComment("name"))
    }
}
