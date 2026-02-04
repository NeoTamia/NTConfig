package re.neotamia.config.json;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;
import re.neotamia.config.format.FormatModule;

/**
 * ServiceLoader provider for registering JSON formats.
 */
public final class JsonFormatModule implements FormatModule {
    /**
     * Creates a Json format module provider.
     */
    public JsonFormatModule() {}

    @Override
    public void register(@NotNull NTConfig config) {
        JsonModule.register(config);
    }
}
