package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main manager for configuration migration operations.
 * Coordinates version detection, backup creation, merging, and callbacks.
 */
public class ConfigMigrationManager {
    private final BackupManager backupManager;
    private final @NotNull ConfigMerger configMerger;
    private final @NotNull List<MigrationHook> hooks;
    private @NotNull MergeStrategy defaultMergeStrategy = MergeStrategy.MERGE_MISSING_ONLY;

    public ConfigMigrationManager(BackupManager backupManager) {
        this.backupManager = backupManager;
        this.configMerger = new ConfigMerger();
        this.hooks = new ArrayList<>();
    }

    public ConfigMigrationManager() {
        this(new BackupManager(Path.of("config-backups")));
    }

    /**
     * Performs migration of a configuration if needed.
     *
     * @param configPath      the path to the configuration file
     * @param loadedConfig    the configuration loaded from file
     * @param currentTemplate the current configuration template with new defaults
     * @param strategy        the merge strategy to use (null to use default)
     * @param <T>             the configuration type
     * @return the migrated configuration, or the original if no migration was needed
     */
    public <T> @NotNull MigrationResult<T> migrate(@NotNull Path configPath, T loadedConfig, @NotNull T currentTemplate, @Nullable MergeStrategy strategy) {
        if (strategy == null) {
            strategy = defaultMergeStrategy;
        }

        try {
            // Extract versions
            MigrationVersion loadedVersion = VersionUtils.extractVersion(loadedConfig);
            MigrationVersion currentVersion = VersionUtils.extractVersion(currentTemplate);

            // If no version fields exist, no migration is possible
            if (loadedVersion == null && currentVersion == null) {
                callHooks(h -> h.onNoMigrationNeeded(configPath, null));
                return new MigrationResult<>(loadedConfig, false, null, null, null);
            }

            // If loaded config has no version, use default from template class
            if (loadedVersion == null) {
                loadedVersion = VersionUtils.getDefaultVersion(currentTemplate.getClass());
                if (loadedVersion == null)
                    loadedVersion = new MigrationVersion("1"); // Fallback
                // Set version in loaded config for consistency
                VersionUtils.setVersion(loadedConfig, loadedVersion);
            }

            // If versions are the same, no migration needed
            if (currentVersion != null && loadedVersion.isEqualTo(currentVersion)) {
                callHooks(h -> h.onNoMigrationNeeded(configPath, currentVersion));
                return new MigrationResult<>(loadedConfig, false, loadedVersion, currentVersion, null);
            }

            // Migration is needed
            final MigrationVersion finalLoadedVersion = loadedVersion;
            final MigrationVersion finalCurrentVersion = currentVersion;
            final MergeStrategy finalStrategy = strategy;

            callHooks(h -> h.beforeMigration(configPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));

            // Create backup
            Path backupPath = null;
            if (backupManager.enabled())
                backupPath = backupManager.createBackup(configPath, loadedVersion);

            final Path finalBackupPath = backupPath;
            callHooks(h -> h.afterBackup(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion));

            // Perform merge
            T mergedConfig = configMerger.merge(loadedConfig, currentTemplate, strategy);

            callHooks(h -> h.afterMigration(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));

            return new MigrationResult<>(mergedConfig, true, loadedVersion, currentVersion, backupPath);

        } catch (Exception e) {
            MigrationVersion loadedVersion = VersionUtils.extractVersion(loadedConfig);
            MigrationVersion currentVersion = VersionUtils.extractVersion(currentTemplate);
            final MergeStrategy finalStrategyForError = strategy;
            callHooks(h -> h.onMigrationFailed(configPath, loadedVersion, currentVersion, finalStrategyForError, e));
            throw new RuntimeException("Migration failed for " + configPath, e);
        }
    }

    /**
     * Convenience method using default merge strategy.
     */
    public <T> MigrationResult<T> migrate(@NotNull Path configPath, T loadedConfig, @NotNull T currentTemplate) {
        return migrate(configPath, loadedConfig, currentTemplate, null);
    }

    /**
     * Checks if migration is needed without performing it.
     *
     * @param loadedConfig    the configuration loaded from file
     * @param currentTemplate the current configuration template
     * @param <T>             the configuration type
     * @return true if migration is needed
     */
    public <T> boolean isMigrationNeeded(T loadedConfig, T currentTemplate) {
        MigrationVersion loadedVersion = VersionUtils.extractVersion(loadedConfig);
        MigrationVersion currentVersion = VersionUtils.extractVersion(currentTemplate);

        if (loadedVersion == null && currentVersion == null) return false;
        if (loadedVersion == null) return true; // Need to add version field
        if (currentVersion == null) return false; // Can't migrate without target version

        return !loadedVersion.isEqualTo(currentVersion);
    }

    public void addHook(@Nullable MigrationHook hook) {
        if (hook != null)
            hooks.add(hook);
    }

    public void removeHook(MigrationHook hook) {
        hooks.remove(hook);
    }

    public void clearHooks() {
        hooks.clear();
    }

    public @NotNull MergeStrategy getDefaultMergeStrategy() {
        return defaultMergeStrategy;
    }

    public void setDefaultMergeStrategy(@Nullable MergeStrategy defaultMergeStrategy) {
        this.defaultMergeStrategy = defaultMergeStrategy != null ? defaultMergeStrategy : MergeStrategy.MERGE_MISSING_ONLY;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    private void callHooks(@NotNull HookConsumer consumer) {
        for (MigrationHook hook : hooks) {
            try {
                consumer.accept(hook);
            } catch (Exception e) {
                // Log hook errors but don't fail migration
                System.err.println("Migration hook failed: " + e.getMessage());
            }
        }
    }

    @FunctionalInterface
    private interface HookConsumer {
        void accept(MigrationHook hook);
    }

    /**
     * Result of a migration operation.
     */
    public record MigrationResult<T>(T config, boolean migrated, MigrationVersion oldVersion, MigrationVersion newVersion, Path backupPath) {
        public boolean wasMigrated() {
            return migrated;
        }

        public boolean hasBackup() {
            return backupPath != null;
        }

        @Override
        public @NotNull String toString() {
            return "MigrationResult{" +
                    "config=" + config +
                    ", migrated=" + migrated +
                    ", oldVersion=" + oldVersion +
                    ", newVersion=" + newVersion +
                    ", backupPath=" + backupPath +
                    '}';
        }
    }
}
