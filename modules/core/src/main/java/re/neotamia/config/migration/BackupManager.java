package re.neotamia.config.migration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages backup creation for configuration files during migration.
 */
public record BackupManager(Path backupDirectory, boolean enabled) {
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public BackupManager(Path backupDirectory) {
        this(backupDirectory, true);
    }

    /**
     * Creates a backup of the given configuration file.
     *
     * @param configPath the path to the configuration file to backup
     * @param version    the current version of the configuration (optional, for naming)
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
     */
    public @Nullable Path createBackup(@NotNull Path configPath, @Nullable MigrationVersion version) throws IOException {
        return this.createBackup(configPath, version != null ? "v_" + version.getVersion() : "");
    }

    /**
     * Creates a backup with a custom suffix.
     *
     * @param configPath the path to the configuration file to backup
     * @param suffix     custom suffix for the backup file name
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
     */
    public @Nullable Path createBackup(@NotNull Path configPath, String suffix) throws IOException {
        if (!enabled || !Files.exists(configPath))
            return null;

        Files.createDirectories(backupDirectory);

        String originalFileName = configPath.getFileName().toString();
        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);

        String backupFileName = getFileNameWithoutExtension(originalFileName)
                + "_" + suffix
                + "_" + timestamp
                + getFileExtension(originalFileName);

        Path backupPath = backupDirectory.resolve(backupFileName);
        Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        return backupPath;
    }

    /**
     * Simple backup with just a timestamp.
     */
    public Path createBackup(@NotNull Path configPath) throws IOException {
        return createBackup(configPath, (MigrationVersion) null);
    }

    /**
     * Restores a configuration file from a backup.
     *
     * @param backupPath the backup file path
     * @param configPath the original configuration file path
     * @throws IOException if the restore fails
     */
    public void restoreBackup(@NotNull Path backupPath, @NotNull Path configPath) throws IOException {
        Files.copy(backupPath, configPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private @NotNull String getFileNameWithoutExtension(@NotNull String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? fileName : fileName.substring(0, lastDot);
    }

    private @NotNull String getFileExtension(@NotNull String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot == -1 ? "" : fileName.substring(lastDot);
    }
}
