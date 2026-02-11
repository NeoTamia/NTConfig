package re.neotamia.config.migration.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.backup.BackupManager;
import re.neotamia.config.migration.hook.MigrationHook;
import re.neotamia.config.migration.step.IConfigMigrationStep;
import re.neotamia.config.migration.version.MigrationVersion;
import re.neotamia.config.migration.version.VersionUtils;
import re.neotamia.config.registry.ConfigMigrationRegistry;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.NamingStrategy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main manager for raw configuration migration operations.
 * Coordinates version detection, backup creation, and callbacks.
 */
public class ConfigMigrationManager {
    private final BackupManager backupManager;
    private final @NotNull ConfigMigrationRegistry migrationRegistry;
    private final @NotNull List<MigrationHook> hooks;
    private MergeStrategy defaultMergeStrategy = MergeStrategy.MERGE_MISSING_ONLY;
    private MissingStepPolicy missingStepPolicy = MissingStepPolicy.FAIL;

    /**
     * Creates a migration manager with a custom backup manager.
     *
     * @param backupManager the backup manager to use
     */
    public ConfigMigrationManager(@NotNull BackupManager backupManager) {
        this.backupManager = backupManager;
        this.migrationRegistry = new ConfigMigrationRegistry();
        this.hooks = new ArrayList<>();
    }

    /**
     * Creates a migration manager using the default backup directory.
     */
    public ConfigMigrationManager() {
        this(new BackupManager(Path.of("config-backups")));
    }

    /**
     * Registers migration steps for the given configuration class.
     *
     * @param clazz the configuration class
     * @param steps the migration steps
     * @param <T>   the configuration type
     */
    public <T> void registerMigrationSteps(@NotNull Class<T> clazz, @NotNull IConfigMigrationStep... steps) {
        migrationRegistry.register(clazz, steps);
    }

    /**
     * Returns the migration registry.
     *
     * @return the registry
     */
    public @NotNull ConfigMigrationRegistry getMigrationRegistry() {
        return migrationRegistry;
    }

    /**
     * Adds a migration hook.
     *
     * @param hook the hook to add
     */
    public void addHook(@Nullable MigrationHook hook) {
        if (hook != null)
            hooks.add(hook);
    }

    /**
     * Removes a migration hook.
     *
     * @param hook the hook to remove
     */
    public void removeHook(MigrationHook hook) {
        hooks.remove(hook);
    }

    /**
     * Clears all registered hooks.
     */
    public void clearHooks() {
        hooks.clear();
    }

    /**
     * Returns the default merge strategy.
     *
     * @return the default merge strategy
     */
    public @NotNull MergeStrategy getDefaultMergeStrategy() {
        return defaultMergeStrategy;
    }

    /**
     * Sets the default merge strategy.
     *
     * @param defaultMergeStrategy the strategy to use
     */
    public void setDefaultMergeStrategy(@Nullable MergeStrategy defaultMergeStrategy) {
        this.defaultMergeStrategy = defaultMergeStrategy != null ? defaultMergeStrategy : MergeStrategy.MERGE_MISSING_ONLY;
    }

    /**
     * Returns the missing step policy.
     *
     * @return the missing step policy
     */
    public @NotNull MissingStepPolicy getMissingStepPolicy() {
        return missingStepPolicy;
    }

    /**
     * Sets the missing step policy.
     *
     * @param missingStepPolicy the policy to use
     */
    public void setMissingStepPolicy(@Nullable MissingStepPolicy missingStepPolicy) {
        this.missingStepPolicy = missingStepPolicy != null ? missingStepPolicy : MissingStepPolicy.FAIL;
    }

    /**
     * Returns the backup manager.
     *
     * @return the backup manager
     */
    public BackupManager getBackupManager() {
        return backupManager;
    }

    /**
     * Performs migration of a raw NightConfig tree using registered migration steps.
     *
     * @param configPath     the path to the configuration file
     * @param rawConfig      the raw config to mutate
     * @param configClass    the configuration class
     * @param currentTemplate the current configuration template
     * @param strategy       the merge strategy (for hooks and backup context)
     * @param namingStrategy the naming strategy used for serialization (nullable)
     * @param <T>            the configuration type
     * @return the raw migration result
     */
    public <T> @NotNull RawMigrationResult migrateRaw(@NotNull Path configPath, @NotNull Config rawConfig, @NotNull Class<T> configClass,
                                                      @NotNull T currentTemplate, @Nullable MergeStrategy strategy, @Nullable NamingStrategy namingStrategy) {
        if (strategy == null) {
            strategy = defaultMergeStrategy;
        }

        MigrationVersion loadedVersion = VersionUtils.extractVersion(rawConfig, configClass, namingStrategy);
        MigrationVersion currentVersion = VersionUtils.extractVersion(currentTemplate);
        if (currentVersion == null) {
            currentVersion = VersionUtils.getDefaultVersion(configClass);
        }

        if (loadedVersion == null) {
            loadedVersion = VersionUtils.getDefaultVersion(configClass);
        }

        if (loadedVersion == null && currentVersion == null) {
            callHooks(h -> h.onNoMigrationNeeded(configPath, null));
            return new RawMigrationResult(rawConfig, false, null, null, null);
        }

        if (loadedVersion == null && currentVersion != null) {
            loadedVersion = new MigrationVersion("1");
        }

        if (currentVersion != null && loadedVersion != null && loadedVersion.isEqualTo(currentVersion)) {
            final MigrationVersion finalCurrentVersion = currentVersion;
            callHooks(h -> h.onNoMigrationNeeded(configPath, finalCurrentVersion));
            return new RawMigrationResult(rawConfig, false, loadedVersion, currentVersion, null);
        }

        if (currentVersion == null || loadedVersion == null) {
            callHooks(h -> h.onNoMigrationNeeded(configPath, null));
            return new RawMigrationResult(rawConfig, false, loadedVersion, currentVersion, null);
        }

        List<IConfigMigrationStep> steps = migrationRegistry.getSteps(configClass);
        if (steps.isEmpty()) {
            if (loadedVersion.isEqualTo(currentVersion)) {
                final MigrationVersion finalCurrentVersion = currentVersion;
                callHooks(h -> h.onNoMigrationNeeded(configPath, finalCurrentVersion));
                return new RawMigrationResult(rawConfig, false, loadedVersion, currentVersion, null);
            }
            final MigrationVersion finalLoadedVersion = loadedVersion;
            final MigrationVersion finalCurrentVersion = currentVersion;
            final MergeStrategy finalStrategy = strategy;
            try {
                callHooks(h -> h.beforeMigration(configPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));
                Path backupPath = null;
                if (backupManager.enabled())
                    backupPath = backupManager.createBackup(configPath, loadedVersion);
                final Path finalBackupPath = backupPath;
                callHooks(h -> h.afterBackup(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion));
                VersionUtils.setVersion(rawConfig, configClass, namingStrategy, currentVersion);
                callHooks(h -> h.afterMigration(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));
                return new RawMigrationResult(rawConfig, true, loadedVersion, currentVersion, backupPath);
            } catch (Exception e) {
                callHooks(h -> h.onMigrationFailed(configPath, finalLoadedVersion, finalCurrentVersion, finalStrategy, e));
                throw new RuntimeException("Migration failed for " + configPath, e);
            }
        }

        MigrationPlan plan = buildPlan(steps, loadedVersion, currentVersion);
        if (!plan.reachedTarget() && missingStepPolicy == MissingStepPolicy.FAIL) {
            throw new RuntimeException("Missing migration step for version " + plan.finalVersion() + " -> " + currentVersion);
        }
        if (plan.steps().isEmpty()) {
            if (missingStepPolicy == MissingStepPolicy.FAIL) {
                throw new RuntimeException("Missing migration step for version " + loadedVersion + " -> " + currentVersion);
            }
            final MigrationVersion finalLoadedVersion = loadedVersion;
            callHooks(h -> h.onNoMigrationNeeded(configPath, finalLoadedVersion));
            return new RawMigrationResult(rawConfig, false, loadedVersion, loadedVersion, null);
        }

        final MigrationVersion finalLoadedVersion = loadedVersion;
        final MigrationVersion finalCurrentVersion = currentVersion;
        final MergeStrategy finalStrategy = strategy;

        try {
            callHooks(h -> h.beforeMigration(configPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));

            Path backupPath = null;
            if (backupManager.enabled())
                backupPath = backupManager.createBackup(configPath, loadedVersion);

            final Path finalBackupPath = backupPath;
            callHooks(h -> h.afterBackup(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion));

            MigrationVersion versionCursor = loadedVersion;
            boolean migrated = false;
            for (IConfigMigrationStep step : plan.steps()) {
                step.migrate(rawConfig);
                migrated = true;
                versionCursor = step.toVersion();
            }

            MigrationVersion resultVersion = versionCursor;
            if (plan.reachedTarget()) {
                VersionUtils.setVersion(rawConfig, configClass, namingStrategy, currentVersion);
                resultVersion = currentVersion;
            }

            callHooks(h -> h.afterMigration(configPath, finalBackupPath, finalLoadedVersion, finalCurrentVersion, finalStrategy));
            return new RawMigrationResult(rawConfig, migrated, loadedVersion, resultVersion, backupPath);

        } catch (Exception e) {
            callHooks(h -> h.onMigrationFailed(configPath, finalLoadedVersion, finalCurrentVersion, finalStrategy, e));
            throw new RuntimeException("Migration failed for " + configPath, e);
        }
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

    private @NotNull MigrationPlan buildPlan(@NotNull List<IConfigMigrationStep> steps, @NotNull MigrationVersion from, @NotNull MigrationVersion target) {
        if (from.isNewerThan(target)) {
            throw new IllegalArgumentException("Cannot migrate from newer version " + from + " to older version " + target);
        }
        Map<MigrationVersion, IConfigMigrationStep> byFrom = new HashMap<>();
        for (IConfigMigrationStep step : steps) {
            IConfigMigrationStep existing = byFrom.put(step.fromVersion(), step);
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate migration step for version " + step.fromVersion());
            }
        }
        List<IConfigMigrationStep> plan = new ArrayList<>();
        MigrationVersion cursor = from;
        boolean reachedTarget = cursor.isEqualTo(target);
        while (!reachedTarget) {
            IConfigMigrationStep step = byFrom.get(cursor);
            if (step == null) break;
            if (!step.toVersion().isNewerThan(cursor)) {
                throw new IllegalArgumentException("Migration step " + step.description() + " does not advance version");
            }
            plan.add(step);
            cursor = step.toVersion();
            reachedTarget = cursor.isEqualTo(target);
        }
        return new MigrationPlan(plan, cursor, reachedTarget);
    }

    private record MigrationPlan(@NotNull List<IConfigMigrationStep> steps, @NotNull MigrationVersion finalVersion, boolean reachedTarget) {}

    /**
     * Result of a migration operation.
     *
     * @param <T>        the configuration type
     * @param config     the migrated configuration instance
     * @param migrated   whether migration was performed
     * @param oldVersion the version before migration
     * @param newVersion the version after migration
     * @param backupPath the backup path, if any
     */
    public record MigrationResult<T>(T config, boolean migrated, MigrationVersion oldVersion, MigrationVersion newVersion, Path backupPath) {
        /**
         * Returns whether migration was performed.
         *
         * @return true if migration occurred
         */
        public boolean wasMigrated() {
            return migrated;
        }

        /**
         * Returns whether a backup was created.
         *
         * @return true if a backup exists
         */
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

    /**
     * Result of a raw config migration operation.
     *
     * @param config     the raw config instance
     * @param migrated   whether migration was performed
     * @param oldVersion the version before migration
     * @param newVersion the version after migration
     * @param backupPath the backup path, if any
     */
    public record RawMigrationResult(@NotNull Config config, boolean migrated, @Nullable MigrationVersion oldVersion,
                                     @Nullable MigrationVersion newVersion, @Nullable Path backupPath) {
        /**
         * Returns whether migration was performed.
         *
         * @return true if migration occurred
         */
        public boolean wasMigrated() {
            return migrated;
        }

        /**
         * Returns whether a backup was created.
         *
         * @return true if a backup exists
         */
        public boolean hasBackup() {
            return backupPath != null;
        }
    }
}
