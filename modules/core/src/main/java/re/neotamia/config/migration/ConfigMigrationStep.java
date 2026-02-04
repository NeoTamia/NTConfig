package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.Config;

/**
 * Defines a migration step that mutates a raw NightConfig {@link Config}.
 */
public interface ConfigMigrationStep {
    /**
     * The version this step migrates from.
     */
    @NotNull MigrationVersion fromVersion();

    /**
     * The version this step migrates to.
     */
    @NotNull MigrationVersion toVersion();

    /**
     * Applies the migration to the provided config.
     *
     * @param config the raw config to mutate
     * @throws Exception if the migration fails
     */
    void migrate(@NotNull Config config) throws Exception;

    /**
     * Optional description for logs/debugging.
     */
    default @NotNull String description() {
        return fromVersion() + " -> " + toVersion();
    }
}
