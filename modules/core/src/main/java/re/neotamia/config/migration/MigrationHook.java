package re.neotamia.config.migration;

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
     * @param strategy the merge strategy being used
     */
    default void beforeMigration(Path configPath, ConfigVersion oldVersion, ConfigVersion newVersion, MergeStrategy strategy) {
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
    default void afterBackup(Path configPath, Path backupPath, ConfigVersion oldVersion, ConfigVersion newVersion) {
        // Default implementation does nothing
    }
    
    /**
     * Called after migration has completed successfully.
     * 
     * @param configPath the path to the configuration file that was migrated
     * @param backupPath the path to the backup file (null if no backup was created)
     * @param oldVersion the previous version of the configuration
     * @param newVersion the new version of the configuration
     * @param strategy the merge strategy that was used
     */
    default void afterMigration(Path configPath, Path backupPath, ConfigVersion oldVersion, ConfigVersion newVersion, MergeStrategy strategy) {
        // Default implementation does nothing
    }
    
    /**
     * Called when migration fails.
     * 
     * @param configPath the path to the configuration file that failed to migrate
     * @param oldVersion the version of the configuration before migration attempt
     * @param newVersion the target version that failed to be reached
     * @param strategy the merge strategy that was being used
     * @param exception the exception that caused the failure
     */
    default void onMigrationFailed(Path configPath, ConfigVersion oldVersion, ConfigVersion newVersion, MergeStrategy strategy, Exception exception) {
        // Default implementation does nothing
    }
    
    /**
     * Called when no migration is needed (versions are the same).
     * 
     * @param configPath the path to the configuration file
     * @param version the current version of the configuration
     */
    default void onNoMigrationNeeded(Path configPath, ConfigVersion version) {
        // Default implementation does nothing
    }
}