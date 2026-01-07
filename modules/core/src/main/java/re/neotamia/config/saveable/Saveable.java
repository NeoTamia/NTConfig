package re.neotamia.config.saveable;

import re.neotamia.nightconfig.core.file.FileConfig;

/**
 * Interface for configuration objects that can save and load themselves
 * using a {@link FileConfig}.
 */
public interface Saveable {
    /**
     * Saves the configuration state to the provided {@link FileConfig}.
     *
     * @param fileConfig the file configuration to save to
     */
    void save(FileConfig fileConfig);

    /**
     * Loads the configuration state from the provided {@link FileConfig}.
     *
     * @param fileConfig the file configuration to load from
     */
    void load(FileConfig fileConfig);
}
