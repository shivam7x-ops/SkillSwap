package com.skillswap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.skillswap.config.FirebaseConfig;
import com.skillswap.model.AuthResult;
import com.skillswap.model.User;
import com.skillswap.service.AuthService;
import com.skillswap.util.ValidationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Live Integration and Verification Test for Firebase Authentication.
 *
 * <p>Verifies:
 * <ol>
 *   <li>API Key loading without exposing values</li>
 *   <li>Field validation</li>
 *   <li>Live registration via Firebase Auth REST API</li>
 *   <li>Realtime Database student profile creation</li>
 *   <li>Duplicate email rejection</li>
 *   <li>Invalid credentials rejection</li>
 *   <li>Live login via Firebase Auth REST API</li>
 *   <li>Firebase Auth user existence via Admin SDK</li>
 * </ol>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FirebaseAuthIntegrationTest {

    private static String testEmail;
    private static String testPassword;
    private static String testName;
    private static String testCollege;
    private static String registeredUid;

    @BeforeAll
    public static void setup() throws Exception {
        System.setProperty("java.awt.headless", "true");

        // Initialize Firebase Admin SDK (used for verifying user creation and Realtime DB)
        FirebaseConfig.initialize();

        // Generate a unique test account
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        testEmail = "test.student." + uniqueSuffix + "@skillswap.edu";
        testPassword = "TestPassword#2026";
        testName = "Test Student " + uniqueSuffix;
        testCollege = "MIT";
    }

    @Test
    @Order(1)
    @DisplayName("1. Verify FIREBASE_API_KEY is loaded securely without exposing value")
    public void testApiKeyLoaded() {
        String apiKey = FirebaseConfig.getApiKey();
        assertNotNull(apiKey, "FIREBASE_API_KEY should be present in configuration");
        assertFalse(apiKey.isBlank(), "FIREBASE_API_KEY should not be empty");
        assertTrue(apiKey.length() >= 30, "FIREBASE_API_KEY should be a valid length");

        // Masked log for terminal verification
        System.out.println("[Test 1/7] API Key Loaded: YES (length: " + apiKey.length() + ", masked: AIzaSy...***)");
    }

    @Test
    @Order(2)
    @DisplayName("2. Client-side field validation")
    public void testFieldValidations() {
        // Bad email
        assertEquals("Please enter a valid email address.", ValidationUtil.validateLogin("invalid-email", "pass1234"));

        // Short password (< 6 chars)
        assertEquals("Password must be at least 6 characters.",
                ValidationUtil.validateRegistration(testName, testEmail, "123", "123", testCollege));

        // Mismatched passwords
        assertEquals("Passwords do not match.",
                ValidationUtil.validateRegistration(testName, testEmail, "password123", "different123", testCollege));

        // Valid form
        assertNull(ValidationUtil.validateRegistration(testName, testEmail, testPassword, testPassword, testCollege));

        System.out.println("[Test 2/7] Field Validations: PASSED");
    }

    @Test
    @Order(3)
    @DisplayName("3. Live Student Registration via Firebase Auth REST API")
    public void testLiveRegistration() throws Exception {
        AuthService authService = AuthService.getInstance();

        AuthResult result = authService.register(testName, testEmail, testPassword, testCollege)
                .get(15, TimeUnit.SECONDS);

        assertTrue(result.isSuccess(), "Registration should succeed: " + result.getMessage());
        assertNotNull(result.getUser(), "User object should be populated");
        assertNotNull(result.getUser().getUid(), "User UID should be returned");
        assertEquals(testEmail, result.getUser().getEmail());
        assertEquals(testName, result.getUser().getName());
        assertEquals(testCollege, result.getUser().getCollege());

        registeredUid = result.getUser().getUid();
        System.out.println("[Test 3/7] Student Registration: SUCCESS (UID created: " + registeredUid + ")");
    }

    @Test
    @Order(4)
    @DisplayName("4. Handle Duplicate Email Registration Error")
    public void testDuplicateEmailHandling() throws Exception {
        AuthService authService = AuthService.getInstance();

        // Attempt registering the same email again
        AuthResult result = authService.register(testName, testEmail, testPassword, testCollege)
                .get(15, TimeUnit.SECONDS);

        assertFalse(result.isSuccess(), "Duplicate email registration must fail");
        assertTrue(result.getMessage().contains("already registered"),
                "Error message should mention already registered: " + result.getMessage());

        System.out.println("[Test 4/7] Duplicate Email Error Handling: PASSED (Message: '" + result.getMessage() + "')");
    }

    @Test
    @Order(5)
    @DisplayName("5. Handle Incorrect Login Credentials Error")
    public void testInvalidPasswordHandling() throws Exception {
        AuthService authService = AuthService.getInstance();

        // Attempt login with incorrect password
        AuthResult result = authService.login(testEmail, "WrongPassword!999")
                .get(15, TimeUnit.SECONDS);

        assertFalse(result.isSuccess(), "Login with wrong password must fail");
        assertTrue(result.getMessage().contains("Invalid email or password") || result.getMessage().contains("Incorrect password"),
                "Error message should indicate invalid credentials: " + result.getMessage());

        System.out.println("[Test 5/7] Incorrect Credentials Error Handling: PASSED (Message: '" + result.getMessage() + "')");
    }

    @Test
    @Order(6)
    @DisplayName("6. Live Student Login via Firebase Auth REST API")
    public void testLiveLogin() throws Exception {
        AuthService authService = AuthService.getInstance();

        AuthResult result = authService.login(testEmail, testPassword)
                .get(15, TimeUnit.SECONDS);

        assertTrue(result.isSuccess(), "Login should succeed: " + result.getMessage());
        assertNotNull(result.getUser(), "User should be loaded from Realtime Database");
        assertEquals(testEmail, result.getUser().getEmail());
        assertEquals(testName, result.getUser().getName());
        assertEquals(testCollege, result.getUser().getCollege());

        System.out.println("[Test 6/7] Student Login: SUCCESS (Welcome message: '" + result.getMessage() + "')");
    }

    @Test
    @Order(7)
    @DisplayName("7. Verify User Record in Firebase Authentication Console/Backend")
    public void testVerifyUserInFirebaseAuth() throws Exception {
        assertNotNull(registeredUid, "UID should have been assigned during registration");

        // Query Firebase Authentication directly via Admin SDK to verify the user exists on Google's servers
        UserRecord userRecord = FirebaseAuth.getInstance().getUser(registeredUid);
        assertNotNull(userRecord, "User record must exist in Firebase Authentication");
        assertEquals(testEmail, userRecord.getEmail());

        System.out.println("[Test 7/7] Firebase Authentication Verification: CONFIRMED on Firebase Backend");
    }
}
