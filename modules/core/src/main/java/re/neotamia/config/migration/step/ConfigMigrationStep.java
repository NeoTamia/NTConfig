package re.neotamia.config.migration.step;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.Config;

import java.util.function.Consumer;

/**
 * Migration step backed by a {@link Consumer} that mutates a raw {@link Config}.
 */
public class ConfigMigrationStep extends AbstractConfigMigrationStep implements IConfigMigrationStep {
    private final Consumer<Config> migrateFunction;

    public ConfigMigrationStep(@NotNull String from, @NotNull String to, @NotNull Consumer<Config> migrateFunction) {
        this(from, to, migrateFunction, null);
    }

    public ConfigMigrationStep(@NotNull String from, @NotNull String to, @NotNull Consumer<Config> migrateFunction, @Nullable String description) {
        super(from, to, description);
        this.migrateFunction = migrateFunction;
    }

    @Override
    public void migrate(@NotNull Config config) throws Exception {
        migrateFunction.accept(config);
    }
}
