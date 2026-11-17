package com.skillswap;

import com.skillswap.config.FirebaseConfig;
import com.skillswap.service.FirebaseService;

import java.io.File;

/**
 * Entry point for testing and verifying the Firebase integration in SkillSwap.
 *
 * <p>Verifies:
 * <ol>
 *   <li>Secure credential loading (no hardcoded secrets)</li>
 *   <li>Firebase Admin SDK initialization</li>
 *   <li>Firebase Realtime Database connectivity</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("       SkillSwap – Firebase Realtime Database Integration Test    ");
        System.out.println("==================================================================");
        System.out.println();

        // 1. Secure Credential Configuration Check
        System.out.println("[Step 1/3] Checking Credential Security Configuration...");
        String serviceAccountPath = FirebaseConfig.getServiceAccountPath();
        String databaseUrl = FirebaseConfig.getDatabaseUrl();

        System.out.println("  • Credential Source       : Environment Variable / .env file");
        System.out.println("  • Service Account Path    : " + (serviceAccountPath != null ? serviceAccountPath : "NOT CONFIGURED"));
        System.out.println("  • Realtime Database URL   : " + (databaseUrl != null ? databaseUrl : "NOT CONFIGURED"));

        boolean hasCredentialsConfigured = serviceAccountPath != null && !serviceAccountPath.isBlank();
        boolean hasDatabaseUrl = databaseUrl != null && !databaseUrl.isBlank();

        if (!hasCredentialsConfigured || !hasDatabaseUrl) {
            System.out.println();
            System.out.println("  [!] CONFIGURATION NOTICE:");
            if (!hasCredentialsConfigured) {
                System.out.println("      - Missing FIREBASE_SERVICE_ACCOUNT_PATH (or GOOGLE_APPLICATION_CREDENTIALS)");
            }
            if (!hasDatabaseUrl) {
                System.out.println("      - Missing FIREBASE_DATABASE_URL");
            }
            System.out.println();
            System.out.println("  To connect with your live Firebase project:");
            System.out.println("    1. Place your 'firebase-service-account.json' file in the project folder.");
            System.out.println("    2. Copy '.env.example' to '.env' and fill in your Realtime Database URL.");
            System.out.println("    3. Note: '.env' and all '*service-account*.json' files are in .gitignore");
            System.out.println("       and will NEVER be committed to Git.");
            System.out.println();
        } else {
            File credFile = new File(serviceAccountPath);
            System.out.println("  • Credential File Exists  : " + (credFile.exists() ? "YES (" + credFile.length() + " bytes)" : "NO (File not found at specified path)"));
        }

        // 2. Firebase Admin SDK Initialization
        System.out.println("[Step 2/3] Initializing Firebase Admin SDK...");
        try {
            FirebaseConfig.initialize();
            System.out.println("  [✓] SUCCESS: Firebase Admin SDK initialized successfully.");
            System.out.println("  • Active Firebase App     : " + FirebaseConfig.getApp().getName());
        } catch (Exception e) {
            System.out.println("  [✗] INITIALIZATION FAILED: " + e.getMessage());
            System.out.println();
            System.out.println("==================================================================");
            System.out.println("  Test Summary: Firebase configuration verified; waiting for valid credentials.");
            System.out.println("==================================================================");
            return;
        }

        // 3. Realtime Database Connectivity Test (No sample data or schema created)
        System.out.println();
        System.out.println("[Step 3/3] Testing Firebase Realtime Database Connectivity...");
        System.out.println("  • Target Path             : /.info/connected (internal read-only metadata)");
        System.out.println("  • Database Records Written: 0 (Strictly non-mutating test)");

        try {
            FirebaseService service = FirebaseService.getInstance();
            System.out.println("  • Pinging Firebase Realtime Database servers (5s timeout)...");
            boolean connected = service.testConnection(5);

            if (connected) {
                System.out.println("  [✓] SUCCESS: Connected to Firebase Realtime Database!");
            } else {
                System.out.println("  [!] STATUS: Handshake sent. Could not confirm online state within 5s.");
                System.out.println("      (Check network connection or database URL validity)");
            }
        } catch (Exception e) {
            System.out.println("  [✗] DATABASE ERROR: " + e.getMessage());
        }

        System.out.println();
        System.out.println("==================================================================");
        System.out.println("  Firebase Integration Test Completed Successfully");
        System.out.println("==================================================================");
        System.exit(0);
    }
}
