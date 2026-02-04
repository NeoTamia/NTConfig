package re.neotamia.config.migration.core;

/**
 * Policy to apply when a migration step is missing.
 */
public enum MissingStepPolicy {
    /**
     * Throw an error if a step is missing.
     */
    FAIL,
    /**
     * Stop migrating when a step is missing.
     */
    SKIP
}
