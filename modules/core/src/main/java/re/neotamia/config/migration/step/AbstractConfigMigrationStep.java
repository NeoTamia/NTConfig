package re.neotamia.config.migration.step;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.migration.version.MigrationVersion;

/**
 * Shared base for migration steps that tracks versions and an optional description.
 */
abstract class AbstractConfigMigrationStep {
    protected final @NotNull MigrationVersion from;
    protected final @NotNull MigrationVersion to;
    protected final @Nullable String description;

    protected AbstractConfigMigrationStep(@NotNull String from, @NotNull String to, @Nullable String description) {
        this(new MigrationVersion(from), new MigrationVersion(to), description);
    }

    protected AbstractConfigMigrationStep(@NotNull MigrationVersion from, @NotNull MigrationVersion to, @Nullable String description) {
        this.from = from;
        this.to = to;
        this.description = description;
    }

    public @NotNull MigrationVersion fromVersion() {
        return from;
    }

    public @NotNull MigrationVersion toVersion() {
        return to;
    }

    public @NotNull String description() {
        return description != null ? description : from + " -> " + to;
    }
}
