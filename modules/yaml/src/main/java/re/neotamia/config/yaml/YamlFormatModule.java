package re.neotamia.config.yaml;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.config.format.FormatModule;

/**
 * ServiceLoader provider for registering YAML formats.
 */
public final class YamlFormatModule implements FormatModule {
    /**
     * Creates a Yaml format module provider.
     */
    public YamlFormatModule() {
    }

    @Override
    public void register(@NotNull NTConfig config) {
        YamlModule.register(config);
    }
}
