package re.neotamia.config.saveable;

import re.neotamia.nightconfig.core.file.CommentedFileConfig;

/**
 * Interface for configuration objects that can save and load themselves
 * using a {@link CommentedFileConfig}, allowing for comments and headers.
 */
public interface SaveableCommented {
    /**
     * Saves the configuration state to the provided {@link CommentedFileConfig}.
     *
     * @param fileConfig the file configuration to save to
     */
    void save(CommentedFileConfig fileConfig);

    /**
     * Loads the configuration state from the provided {@link CommentedFileConfig}.
     *
     * @param fileConfig the file configuration to load from
     */
    void load(CommentedFileConfig fileConfig);
}
