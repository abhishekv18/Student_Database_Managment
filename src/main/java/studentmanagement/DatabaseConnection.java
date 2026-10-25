package studentmanagement;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Creates connections to the MySQL database.
 *
 * The database URL, username and password are NOT written in the code.
 * They are read from a file named "db.properties" in the project folder,
 * so every user can enter their own MySQL details and the password is
 * never committed to Git.
 */
public class DatabaseConnection {

    // Name of the settings file. It must be in the project's root folder.
    private static final String CONFIG_FILE = "db.properties";

    /**
     * Opens and returns a new connection to MySQL.
     * The caller must close the connection (we use try-with-resources for that).
     */
    public static Connection getConnection() throws SQLException {
        Properties settings = loadSettings();

        String url = settings.getProperty("db.url");
        String username = settings.getProperty("db.username");
        String password = settings.getProperty("db.password", "");

        if (url == null || username == null) {
            throw new SQLException("db.properties must contain db.url and db.username.");
        }

        // DriverManager finds the MySQL driver (Connector/J) automatically.
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Reads db.properties and returns its key=value settings.
     */
    private static Properties loadSettings() throws SQLException {
        Properties settings = new Properties();

        // try-with-resources closes the file automatically, even if an error happens.
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            settings.load(input);
        } catch (IOException e) {
            throw new SQLException("Could not read " + CONFIG_FILE + ". Copy db.properties.example to "
                    + CONFIG_FILE + " in the project folder and enter your MySQL details.");
        }

        return settings;
    }
}
