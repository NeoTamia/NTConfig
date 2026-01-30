package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a configuration version that can be compared for migration purposes.
 * Supports both integer and semantic versioning.
 */
public class MigrationVersion implements Comparable<MigrationVersion> {
    private final @NotNull String version;
    private final int major;
    private final int minor;
    private final int patch;
    private final boolean isSemanticVersion;

    public MigrationVersion(@NotNull String version) {
        this.version = Objects.requireNonNull(version, "Version cannot be null");

        // Try to parse as semantic version (major.minor.patch)
        if (version.matches("\\d+\\.\\d+\\.\\d+")) {
            String[] parts = version.split("\\.");
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.patch = Integer.parseInt(parts[2]);
            this.isSemanticVersion = true;
        } else if (version.matches("\\d+")) {
            // Integer version
            this.major = Integer.parseInt(version);
            this.minor = 0;
            this.patch = 0;
            this.isSemanticVersion = false;
        } else {
            throw new IllegalArgumentException("Version must be either an integer or semantic version (major.minor.patch): " + version);
        }
    }

    public MigrationVersion(int version) {
        this.version = String.valueOf(version);
        this.major = version;
        this.minor = 0;
        this.patch = 0;
        this.isSemanticVersion = false;
    }

    public MigrationVersion(int major, int minor, int patch) {
        this.version = major + "." + minor + "." + patch;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.isSemanticVersion = true;
    }

    public @NotNull String getVersion() {
        return version;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public boolean isSemanticVersion() {
        return isSemanticVersion;
    }

    @Override
    public int compareTo(@NotNull MigrationVersion other) {
        if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
        }
        if (this.minor != other.minor) {
            return Integer.compare(this.minor, other.minor);
        }
        return Integer.compare(this.patch, other.patch);
    }

    public boolean isNewerThan(@NotNull MigrationVersion other) {
        return this.compareTo(other) > 0;
    }

    public boolean isOlderThan(@NotNull MigrationVersion other) {
        return this.compareTo(other) < 0;
    }

    public boolean isEqualTo(@NotNull MigrationVersion other) {
        return this.compareTo(other) == 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MigrationVersion that = (MigrationVersion) o;
        return major == that.major && minor == that.minor && patch == that.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch);
    }

    @Override
    public String toString() {
        return version;
    }
}
