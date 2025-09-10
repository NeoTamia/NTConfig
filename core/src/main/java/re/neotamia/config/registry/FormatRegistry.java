package re.neotamia.config.registry;

import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.file.FormatDetector;

import java.util.ArrayList;
import java.util.List;

public class FormatRegistry {
    private final List<ConfigFormat<?>> formats = new ArrayList<>();

    public void register(ConfigFormat<?> format, String... extensions) {
        formats.add(format);
        for (String extension : extensions)
            FormatDetector.registerExtension(extension, format);
    }

    public List<ConfigFormat<?>> getFormats() {
        return formats;
    }
}
