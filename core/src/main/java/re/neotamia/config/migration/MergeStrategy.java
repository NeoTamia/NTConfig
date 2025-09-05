package re.neotamia.config.migration;

/**
 * Defines strategies for merging/updating configuration files.
 */
public enum MergeStrategy {
    /**
     * Only add missing fields from the new configuration, keep existing values unchanged.
     */
    MERGE_MISSING_ONLY,
    
    /**
     * Add missing fields and update existing fields with new default values.
     */
    MERGE_WITH_UPDATES,
    
    /**
     * Completely replace the old configuration with the new one.
     */
    OVERRIDE,
    
    /**
     * Keep existing configuration as-is, only update the version field.
     */
    VERSION_ONLY,
    
    /**
     * Custom merge strategy - requires a custom merger implementation.
     */
    CUSTOM
}