package re.neotamia.config.registry;

import org.jetbrains.annotations.NotNull;
import re.neotamia.config.migration.step.IConfigMigrationStep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that maps configuration classes to their migration steps.
 */
public final class ConfigMigrationRegistry {
    private final @NotNull Map<Class<?>, List<IConfigMigrationStep>> stepsByClass = new ConcurrentHashMap<>();

    /**
     * Creates a new migration registry.
     */
    public ConfigMigrationRegistry() {}

    /**
     * Registers one or more migration steps for the given configuration class.
     *
     * @param clazz the configuration class
     * @param steps the steps to register
     * @param <T>   the configuration type
     */
    public <T> void register(@NotNull Class<T> clazz, @NotNull IConfigMigrationStep... steps) {
        if (steps.length == 0) return;
        stepsByClass.computeIfAbsent(clazz, ignored -> new ArrayList<>()).addAll(Arrays.asList(steps));
    }

    /**
     * Returns the migration steps registered for the given configuration class.
     *
     * @param clazz the configuration class
     * @return an immutable list of steps, possibly empty
     */
    public @NotNull List<IConfigMigrationStep> getSteps(@NotNull Class<?> clazz) {
        List<IConfigMigrationStep> steps = stepsByClass.get(clazz);
        if (steps == null || steps.isEmpty()) return Collections.emptyList();
        return List.copyOf(steps);
    }

    /**
     * Clears all migration steps registered for the given configuration class.
     *
     * @param clazz the configuration class
     */
    public void clear(@NotNull Class<?> clazz) {
        stepsByClass.remove(clazz);
    }

    /**
     * Clears all registered migration steps.
     */
    public void clearAll() {
        stepsByClass.clear();
    }
}
