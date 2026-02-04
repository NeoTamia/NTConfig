package re.neotamia.config.toml;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.config.format.FormatModule;

/**
 * ServiceLoader provider for registering TOML formats.
 */
public final class TomlFormatModule implements FormatModule {
    /**
     * Creates a Toml format module provider.
     */
    public TomlFormatModule() {
    }

    @Override
    public void register(@NotNull NTConfig config) {
        TomlModule.register(config);
    }
}
