package re.neotamia.config.registry;

import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.file.FormatDetector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Registry for supported configuration formats.
 */
public class FormatRegistry {
    private final List<ConfigFormat<?>> formats = new ArrayList<>();
    private final Set<String> registeredExtensions = new HashSet<>();

    /**
     * Creates a new format registry.
     */
    public FormatRegistry() {}

    /**
     * Registers a format and its file extensions.
     *
     * @param format     the format to register
     * @param extensions file extensions for the format
     */
    public void register(ConfigFormat<?> format, String... extensions) {
        if (!formats.contains(format))
            formats.add(format);
        for (String extension : extensions) {
            if (registeredExtensions.add(extension))
                FormatDetector.registerExtension(extension, format);
        }
    }

    /**
     * Returns registered formats.
     *
     * @return the list of formats
     */
    public List<ConfigFormat<?>> getFormats() {
        return formats;
    }
}
