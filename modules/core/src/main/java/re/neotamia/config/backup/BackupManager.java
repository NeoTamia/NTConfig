package re.neotamia.config.backup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.config.migration.version.MigrationVersion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages backup creation for configuration files during migration.
 *
 * @param backupDirectory the directory where backups are stored
 * @param enabled         whether backups are enabled
 */
public record BackupManager(@NotNull Path backupDirectory, boolean enabled) {
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Creates a backup manager with backups enabled.
     *
     * @param backupDirectory the directory where backups are stored
     */
    public BackupManager(@NotNull Path backupDirectory) {
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
        return this.createBackup(configPath, version != null ? "v" + version.getVersion() : null);
    }

    /**
     * Creates a backup with a custom suffix.
     *
     * @param configPath the path to the configuration file to backup
     * @param suffix     custom suffix for the backup file name
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
     */
    public @Nullable Path createBackup(@NotNull Path configPath, @Nullable String suffix) throws IOException {
        if (!enabled || !Files.exists(configPath))
            return null;

        Files.createDirectories(backupDirectory);

        String originalFileName = configPath.getFileName().toString();
        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
        String baseName = getFileNameWithoutExtension(originalFileName);
        String extension = getFileExtension(originalFileName);

        StringBuilder sb = new StringBuilder(baseName);
        if (suffix != null)
            sb.append("_").append(suffix);
        sb.append("_").append(timestamp);
        sb.append(extension);

        String backupFileName = sb.toString();
        Path backupPath = backupDirectory.resolve(backupFileName);
        Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        return backupPath;
    }

    /**
     * Simple backup with just a timestamp.
     *
     * @param configPath the path to the configuration file to backup
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
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
