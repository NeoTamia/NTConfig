package re.neotamia.config.migration.step;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;

/**
 * Migration step that operates on {@link CommentedConfig} to allow comment updates.
 */
public interface CommentedConfigMigrationStep extends ConfigMigrationStep {
    /**
     * Applies the migration to the provided commented config.
     *
     * @param config the commented config to mutate
     * @throws Exception if the migration fails
     */
    void migrate(@NotNull CommentedConfig config) throws Exception;

    @Override
    default void migrate(@NotNull Config config) throws Exception {
        if (!(config instanceof CommentedConfig commentedConfig)) {
            throw new IllegalArgumentException("CommentedConfigMigrationStep requires a CommentedConfig");
        }
        migrate(commentedConfig);
    }
}
