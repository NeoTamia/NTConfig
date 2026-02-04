package re.neotamia.config.registry;

import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.file.FormatDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry for supported configuration formats.
 */
public class FormatRegistry {
    private final List<ConfigFormat<?>> formats = new ArrayList<>();

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
        formats.add(format);
        for (String extension : extensions)
            FormatDetector.registerExtension(extension, format);
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
