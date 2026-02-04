package re.neotamia.config.format;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.NTConfig;

import java.util.ServiceLoader;

/**
 * Utility to load and register available format modules via {@link ServiceLoader}.
 */
public final class FormatModules {
    /**
     * Utility class.
     */
    private FormatModules() {}

    /**
     * Registers all format modules available on the classpath.
     *
     * @param config the NTConfig instance to register on
     */
    public static void registerAvailable(@NotNull NTConfig config) {
        ServiceLoader<FormatModule> loader = ServiceLoader.load(FormatModule.class);
        for (FormatModule module : loader) {
            module.register(config);
        }
    }
}
