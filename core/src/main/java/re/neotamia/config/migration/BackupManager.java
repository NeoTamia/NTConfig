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
public class BackupManager {
    private static final DateTimeFormatter BACKUP_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final Path backupDirectory;
    private final boolean enabled;

    public BackupManager(Path backupDirectory, boolean enabled) {
        this.backupDirectory = backupDirectory;
        this.enabled = enabled;
    }

    public BackupManager(Path backupDirectory) {
        this(backupDirectory, true);
    }

    /**
     * Creates a backup of the given configuration file.
     * 
     * @param configPath the path to the configuration file to backup
     * @param version the current version of the configuration (optional, for naming)
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
     */
    public @Nullable Path createBackup(@NotNull Path configPath, @Nullable ConfigVersion version) throws IOException {
        if (!enabled) {
            return null;
        }

        if (!Files.exists(configPath)) {
            return null; // Nothing to backup
        }

        // Create backup directory if it doesn't exist
        Files.createDirectories(backupDirectory);

        // Generate backup filename
        String originalFileName = configPath.getFileName().toString();
        String timestamp = LocalDateTime.now().format(BACKUP_DATE_FORMAT);
        String versionSuffix = version != null ? "_v" + version.getVersion() : "";
        
        String backupFileName = getFileNameWithoutExtension(originalFileName) 
            + versionSuffix 
            + "_backup_" + timestamp 
            + getFileExtension(originalFileName);

        Path backupPath = backupDirectory.resolve(backupFileName);

        // Copy the file
        Files.copy(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        return backupPath;
    }

    /**
     * Creates a backup with a custom suffix.
     * 
     * @param configPath the path to the configuration file to backup
     * @param suffix custom suffix for the backup file name
     * @return the path to the created backup file, or null if backups are disabled
     * @throws IOException if the backup creation fails
     */
    public @Nullable Path createBackup(@NotNull Path configPath, String suffix) throws IOException {
        if (!enabled) {
            return null;
        }

        if (!Files.exists(configPath)) {
            return null;
        }

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
     * Simple backup with just timestamp.
     */
    public Path createBackup(@NotNull Path configPath) throws IOException {
        return createBackup(configPath, (ConfigVersion) null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Path getBackupDirectory() {
        return backupDirectory;
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