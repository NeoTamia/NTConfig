package re.neotamia.config.format;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;

/**
 * Service-provider hook for registering configuration formats.
 */
public interface FormatModule {
    /**
     * Registers one or more formats on the given {@link NTConfig} instance.
     *
     * @param config the target NTConfig instance
     */
    void register(@NotNull NTConfig config);
}
