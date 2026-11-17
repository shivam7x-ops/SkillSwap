package com.skillswap.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralised Firebase configuration and initialization bootstrap.
 *
 * <p>Loads credentials securely following a prioritized hierarchy:
 * <ol>
 *   <li>System Environment Variables (e.g. {@code FIREBASE_SERVICE_ACCOUNT_PATH}, {@code GOOGLE_APPLICATION_CREDENTIALS})</li>
 *   <li>JVM System Properties (e.g. {@code -Dfirebase.service.account.path=...})</li>
 *   <li>External configuration file ({@code .env}) via Dotenv</li>
 * </ol>
 *
 * <p>Credentials and secret keys are never hardcoded in source code or pushed to Git.
 */
public final class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    private static final String ENV_SERVICE_ACCOUNT_PATH = "FIREBASE_SERVICE_ACCOUNT_PATH";
    private static final String ENV_GOOGLE_APP_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";
    private static final String ENV_DATABASE_URL = "FIREBASE_DATABASE_URL";
    private static final String ENV_API_KEY = "FIREBASE_API_KEY";

    private static final Object LOCK = new Object();
    private static Dotenv dotenv;
    private static volatile boolean initialized = false;
    private static FirebaseApp firebaseApp;
    private static FirebaseDatabase firebaseDatabase;

    private FirebaseConfig() {
        // Utility class
    }

    /**
     * Initialize Firebase with service account credentials and Realtime Database URL.
     * Safe to call multiple times; subsequent calls are idempotent.
     *
     * @throws IOException              if service account credentials cannot be read
     * @throws IllegalArgumentException if required configurations are missing or invalid
     * @throws IllegalStateException    if Firebase fails to initialize
     */
    public static void initialize() throws IOException {
        if (initialized) {
            return;
        }

        synchronized (LOCK) {
            if (initialized) {
                return;
            }

            logger.info("Initializing Firebase Admin SDK...");
            loadDotenv();

            String credentialsPath = resolveConfig(ENV_SERVICE_ACCOUNT_PATH);
            if (credentialsPath == null || credentialsPath.isBlank()) {
                credentialsPath = resolveConfig(ENV_GOOGLE_APP_CREDENTIALS);
            }

            String databaseUrl = resolveConfig(ENV_DATABASE_URL);

            if (credentialsPath == null || credentialsPath.isBlank()) {
                throw new IllegalArgumentException(
                        "Firebase service account path is not configured. "
                                + "Please provide '" + ENV_SERVICE_ACCOUNT_PATH + "' or '"
                                + ENV_GOOGLE_APP_CREDENTIALS + "' in environment variables or .env file.");
            }

            if (databaseUrl == null || databaseUrl.isBlank()) {
                throw new IllegalArgumentException(
                        "Firebase Database URL is not configured. "
                                + "Please provide '" + ENV_DATABASE_URL + "' in environment variables or .env file.");
            }

            if (!databaseUrl.startsWith("https://")) {
                throw new IllegalArgumentException(
                        "Invalid Firebase Database URL: '" + databaseUrl + "'. It must start with 'https://'.");
            }

            Path path = Paths.get(credentialsPath);
            if (!Files.exists(path) || !Files.isReadable(path)) {
                // Also check relative to current working directory or user home
                File file = new File(credentialsPath);
                if (!file.exists() || !file.canRead()) {
                    throw new IOException(
                            "Firebase service account file not found or not readable at: "
                                    + path.toAbsolutePath());
                }
            }

            try (InputStream serviceAccountStream = new FileInputStream(path.toFile())) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .setDatabaseUrl(databaseUrl.trim())
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    firebaseApp = FirebaseApp.initializeApp(options);
                } else {
                    firebaseApp = FirebaseApp.getInstance();
                }

                firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
                initialized = true;
                logger.info("Firebase Realtime Database initialized successfully with target: {}", databaseUrl);
            } catch (Exception e) {
                logger.error("Failed to initialize Firebase Admin SDK", e);
                throw new IOException("Firebase initialization failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the connected {@link FirebaseDatabase} instance.
     * Automatically initializes Firebase if not already initialized.
     *
     * @return initialized FirebaseDatabase
     */
    public static FirebaseDatabase getDatabase() {
        if (!initialized) {
            try {
                initialize();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot access FirebaseDatabase: initialization failed", e);
            }
        }
        return firebaseDatabase;
    }

    /**
     * Gets the active {@link FirebaseApp} instance.
     */
    public static FirebaseApp getApp() {
        if (!initialized) {
            try {
                initialize();
            } catch (IOException e) {
                throw new IllegalStateException("Cannot access FirebaseApp: initialization failed", e);
            }
        }
        return firebaseApp;
    }

    /**
     * Retrieves the Firebase Web API key if configured.
     *
     * @return Firebase Web API key or null if not set
     */
    public static String getApiKey() {
        loadDotenv();
        String key = resolveConfig(ENV_API_KEY);
        if (key == null || key.isBlank()) {
            // Refresh dotenv in case .env was recently created or updated on disk
            reloadDotenv();
            key = resolveConfig(ENV_API_KEY);
        }
        return key;
    }

    /**
     * Forces a reload of the .env file from disk.
     */
    public static void reloadDotenv() {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
        } catch (Exception e) {
            logger.warn("Could not reload .env file: {}", e.getMessage());
        }
    }

    /**
     * Checks whether Firebase has been successfully initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the configured Firebase Database URL without initializing if not yet done.
     */
    public static String getDatabaseUrl() {
        loadDotenv();
        return resolveConfig(ENV_DATABASE_URL);
    }

    /**
     * Returns the configured Service Account path without initializing if not yet done.
     */
    public static String getServiceAccountPath() {
        loadDotenv();
        String path = resolveConfig(ENV_SERVICE_ACCOUNT_PATH);
        if (path == null || path.isBlank()) {
            path = resolveConfig(ENV_GOOGLE_APP_CREDENTIALS);
        }
        return path;
    }

    /**
     * Resolves a configuration value from Environment Variables, System Properties, or Dotenv.
     */
    private static String resolveConfig(String key) {
        // 1. Check OS environment variable
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return cleanValue(value);
        }

        // 2. Check Java system property (e.g. -Dfirebase.database.url=...)
        String propKey = key.toLowerCase().replace('_', '.');
        value = System.getProperty(propKey);
        if (value != null && !value.isBlank()) {
            return cleanValue(value);
        }

        // 3. Check .env file
        if (dotenv != null) {
            value = dotenv.get(key);
            if (value != null && !value.isBlank()) {
                return cleanValue(value);
            }
        }

        return null;
    }

    private static String cleanValue(String val) {
        if (val == null) return null;
        val = val.trim();
        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
            if (val.length() >= 2) {
                val = val.substring(1, val.length() - 1).trim();
            }
        }
        return val;
    }

    private static void loadDotenv() {
        if (dotenv == null) {
            try {
                dotenv = Dotenv.configure()
                        .ignoreIfMissing()
                        .load();
            } catch (Exception e) {
                logger.warn("Could not load .env file: {}", e.getMessage());
            }
        }
    }
}
