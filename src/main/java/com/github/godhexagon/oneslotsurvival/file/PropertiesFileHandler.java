package com.github.godhexagon.oneslotsurvival.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Generic handler for Properties file I/O operations.
 * Handles reading and writing Properties files in the config directory.
 */
public class PropertiesFileHandler {
    private final String fileName;
    private final String comment;

    /**
     * Create a new PropertiesFileHandler.
     * @param fileName the name of the properties file (e.g., "settings.properties")
     * @param comment the comment to write at the top of the file when saving
     */
    public PropertiesFileHandler(String fileName, String comment) {
        this.fileName = fileName;
        this.comment = comment;
    }

    /**
     * Load properties from file.
     * Returns an empty Properties object if the file doesn't exist or cannot be read.
     * @return the loaded properties, or an empty Properties if loading fails
     */
    public Properties load() {
        Properties props = new Properties();
        File dataFile = getDataFile();

        if (!dataFile.exists()) {
            return props;
        }

        try (FileInputStream fis = new FileInputStream(dataFile)) {
            props.load(fis);
        } catch (IOException e) {
            // Return empty properties on failure
        }

        return props;
    }

    /**
     * Save properties to file.
     * Creates parent directories if they don't exist.
     * @param props the properties to save
     * @return true if save was successful, false otherwise
     */
    public boolean save(Properties props) {
        File dataFile = getDataFile();

        // Ensure parent directory exists
        File parentDir = dataFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            props.store(fos, comment);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the data file.
     * @return the File object for the properties file
     */
    private File getDataFile() {
        File configDir = new File(System.getProperty("user.dir"), "config");
        configDir.mkdirs();
        return new File(configDir, fileName);
    }

    /**
     * Check if the data file exists.
     * @return true if the file exists, false otherwise
     */
    public boolean exists() {
        return getDataFile().exists();
    }
}