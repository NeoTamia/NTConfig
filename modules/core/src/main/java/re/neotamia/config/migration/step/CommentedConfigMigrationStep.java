package re.neotamia.config.migration.step;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.CommentedConfig;

import java.util.function.Consumer;

/**
 * Migration step backed by a {@link Consumer} that mutates a {@link CommentedConfig}.
 */
public class CommentedConfigMigrationStep extends AbstractConfigMigrationStep implements ICommentedConfigMigrationStep {
    private final Consumer<CommentedConfig> migrateFunction;

    public CommentedConfigMigrationStep(@NotNull String from, @NotNull String to, @NotNull Consumer<CommentedConfig> migrateFunction) {
        this(from, to, migrateFunction, null);
    }

    public CommentedConfigMigrationStep(@NotNull String from, @NotNull String to, @NotNull Consumer<CommentedConfig> migrateFunction,
                                        @Nullable String description) {
        super(from, to, description);
        this.migrateFunction = migrateFunction;
    }

    @Override
    public void migrate(@NotNull CommentedConfig config) throws Exception {
        migrateFunction.accept(config);
    }
}
