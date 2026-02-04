package re.neotamia.config.migration.step;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.migration.version.MigrationVersion;
import re.neotamia.nightconfig.core.Config;

/**
 * Defines a migration step that mutates a raw NightConfig {@link Config}.
 */
public interface ConfigMigrationStep {
    /**
     * The version this step migrates from.
     *
     * @return the source version
     */
    @NotNull MigrationVersion fromVersion();

    /**
     * The version this step migrates to.
     *
     * @return the target version
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
     *
     * @return the description string
     */
    default @NotNull String description() {
        return fromVersion() + " -> " + toVersion();
    }
}
