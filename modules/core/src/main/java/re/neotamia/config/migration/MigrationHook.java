package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Interface for migration hooks that can be executed during configuration migration.
 * Allows custom logic to be run at different stages of the migration process.
 */
public interface MigrationHook {

    /**
     * Called before migration starts.
     *
     * @param configPath the path to the configuration file being migrated
     * @param oldVersion the current version of the configuration
     * @param newVersion the target version for migration
     * @param strategy   the merge strategy being used
     */
    default void beforeMigration(@NotNull Path configPath, @Nullable MigrationVersion oldVersion, @NotNull MigrationVersion newVersion, @NotNull MergeStrategy strategy) {
        // Default implementation does nothing
    }

    /**
     * Called after a backup has been created but before the actual migration.
     *
     * @param configPath the path to the configuration file being migrated
     * @param backupPath the path to the created backup file (null if no backup was created)
     * @param oldVersion the current version of the configuration
     * @param newVersion the target version for migration
     */
    default void afterBackup(@NotNull Path configPath, @NotNull Path backupPath, @Nullable MigrationVersion oldVersion, @NotNull MigrationVersion newVersion) {
        // Default implementation does nothing
    }

    /**
     * Called after migration has completed successfully.
     *
     * @param configPath the path to the configuration file that was migrated
     * @param backupPath the path to the backup file (null if no backup was created)
     * @param oldVersion the previous version of the configuration
     * @param newVersion the new version of the configuration
     * @param strategy   the merge strategy that was used
     */
    default void afterMigration(@NotNull Path configPath, @NotNull Path backupPath, @Nullable MigrationVersion oldVersion, @NotNull MigrationVersion newVersion, @NotNull MergeStrategy strategy) {
        // Default implementation does nothing
    }

    /**
     * Called when migration fails.
     *
     * @param configPath the path to the configuration file that failed to migrate
     * @param oldVersion the version of the configuration before migration attempt
     * @param newVersion the target version that failed to be reached
     * @param strategy   the merge strategy that was being used
     * @param exception  the exception that caused the failure
     */
    default void onMigrationFailed(@NotNull Path configPath, @Nullable MigrationVersion oldVersion, @NotNull MigrationVersion newVersion, @NotNull MergeStrategy strategy, @NotNull Exception exception) {
        // Default implementation does nothing
    }

    /**
     * Called when no migration is needed (versions are the same).
     *
     * @param configPath the path to the configuration file
     * @param version    the current version of the configuration
     */
    default void onNoMigrationNeeded(@NotNull Path configPath, @Nullable MigrationVersion version) {
        // Default implementation does nothing
    }
}
