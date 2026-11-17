package com.skillswap.service;

import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skillswap.config.FirebaseConfig;
import com.skillswap.model.AuthResult;
import com.skillswap.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Service handling Firebase Authentication operations via Firebase REST API.
 *
 * <p>Uses the Firebase Auth REST endpoints to register and authenticate students
 * using email and password, completely isolated from GUI code.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>Sign Up: {@code https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={API_KEY}}</li>
 *   <li>Sign In: {@code https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={API_KEY}}</li>
 * </ul>
 *
 * <p>Student profiles are persisted in Firebase Realtime Database at {@code /users/{uid}}.
 */
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String SIGN_UP_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=";
    private static final String SIGN_IN_URL =
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=";

    private static volatile AuthService instance;

    private final HttpClient httpClient;
    private final Gson gson;
    private final FirebaseService firebaseService;

    private volatile User currentUser;
    private volatile String currentIdToken;

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.firebaseService = FirebaseService.getInstance();
    }

    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a new student using Firebase Authentication REST API and creates
     * their initial profile in Firebase Realtime Database.
     *
     * @param name     student's full name
     * @param email    student's email address
     * @param password student's password
     * @param college  student's college/university
     * @return CompletableFuture with AuthResult
     */
    public CompletableFuture<AuthResult> register(String name, String email, String password, String college) {
        String apiKey = FirebaseConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.completedFuture(
                    AuthResult.failure("Firebase Web API Key is not configured. Please add 'FIREBASE_API_KEY' to your .env file."));
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email.trim().toLowerCase());
        payload.addProperty("password", password);
        payload.addProperty("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_UP_URL + apiKey.trim()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    int statusCode = response.statusCode();
                    String body = response.body();

                    if (statusCode == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String uid = json.get("localId").getAsString();
                            String idToken = json.get("idToken").getAsString();

                            // Construct Student User Model
                            User newUser = new User(uid, name.trim(), email.trim(), college.trim());
                            newUser.setSkillsToTeach(Collections.emptyList());
                            newUser.setSkillsToLearn(Collections.emptyList());

                            // Store student profile in Realtime Database under /users/{uid}
                            return firebaseService.setValue("users/" + uid, newUser.toMap())
                                    .thenApply(v -> {
                                        this.currentUser = newUser;
                                        this.currentIdToken = idToken;
                                        logger.info("Student registered successfully: {} ({})", newUser.getEmail(), uid);
                                        return AuthResult.success("Registration successful! Welcome to SkillSwap.", newUser, idToken);
                                    })
                                    .exceptionally(dbErr -> {
                                        logger.error("User authenticated but profile save failed in DB", dbErr);
                                        // Still return user object so the session is established
                                        this.currentUser = newUser;
                                        this.currentIdToken = idToken;
                                        return AuthResult.success("Registration succeeded with warnings: profile saved partially.", newUser, idToken);
                                    });
                        } catch (Exception e) {
                            logger.error("Failed to parse registration response", e);
                            return CompletableFuture.completedFuture(
                                    AuthResult.failure("Could not parse registration response: " + e.getMessage()));
                        }
                    } else {
                        String errorMessage = parseFirebaseErrorMessage(body, "Registration failed");
                        logger.warn("Firebase Auth registration rejected: {}", errorMessage);
                        return CompletableFuture.completedFuture(AuthResult.failure(errorMessage));
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Network or connection error during registration", ex);
                    return AuthResult.failure("Network error: " + ex.getMessage());
                });
    }

    /**
     * Signs in an existing student using Firebase Authentication REST API and fetches
     * their profile from Firebase Realtime Database.
     *
     * @param email    student's email address
     * @param password student's password
     * @return CompletableFuture with AuthResult
     */
    public CompletableFuture<AuthResult> login(String email, String password) {
        String apiKey = FirebaseConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return CompletableFuture.completedFuture(
                    AuthResult.failure("Firebase Web API Key is not configured. Please add 'FIREBASE_API_KEY' to your .env file."));
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("email", email.trim().toLowerCase());
        payload.addProperty("password", password);
        payload.addProperty("returnSecureToken", true);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SIGN_IN_URL + apiKey.trim()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    int statusCode = response.statusCode();
                    String body = response.body();

                    if (statusCode == 200) {
                        try {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            String uid = json.get("localId").getAsString();
                            String idToken = json.get("idToken").getAsString();
                            String returnedEmail = json.has("email") ? json.get("email").getAsString() : email;

                            // Fetch complete profile from Realtime Database
                            return firebaseService.getData("users/" + uid)
                                    .thenApply(snapshot -> {
                                        User user = null;
                                        if (snapshot.exists()) {
                                            user = snapshot.getValue(User.class);
                                        }
                                        if (user == null) {
                                            // Fallback profile if DB node doesn't exist yet
                                            user = new User(uid, returnedEmail.split("@")[0], returnedEmail, "Not specified");
                                        }

                                        this.currentUser = user;
                                        this.currentIdToken = idToken;
                                        logger.info("Student signed in successfully: {} ({})", user.getEmail(), uid);
                                        return AuthResult.success("Login successful! Welcome back, " + user.getName() + ".", user, idToken);
                                    })
                                    .exceptionally(dbErr -> {
                                        logger.warn("Could not retrieve user profile from DB: {}", dbErr.getMessage());
                                        User fallbackUser = new User(uid, returnedEmail.split("@")[0], returnedEmail, "Not specified");
                                        this.currentUser = fallbackUser;
                                        this.currentIdToken = idToken;
                                        return AuthResult.success("Login successful!", fallbackUser, idToken);
                                    });
                        } catch (Exception e) {
                            logger.error("Failed to parse login response", e);
                            return CompletableFuture.completedFuture(
                                    AuthResult.failure("Failed to parse login response: " + e.getMessage()));
                        }
                    } else {
                        String errorMessage = parseFirebaseErrorMessage(body, "Login failed");
                        logger.warn("Firebase Auth login rejected: {}", errorMessage);
                        return CompletableFuture.completedFuture(AuthResult.failure(errorMessage));
                    }
                })
                .exceptionally(ex -> {
                    logger.error("Network or connection error during login", ex);
                    return AuthResult.failure("Network error: " + ex.getMessage());
                });
    }

    /**
     * Signs out the current student by clearing the local session.
     */
    public void logout() {
        this.currentUser = null;
        this.currentIdToken = null;
        logger.info("User signed out.");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentIdToken() {
        return currentIdToken;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Parses Firebase REST API error responses and maps error codes to friendly messages.
     */
    private String parseFirebaseErrorMessage(String responseBody, String defaultMessage) {
        if (responseBody == null || responseBody.isBlank()) {
            return defaultMessage;
        }

        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (json.has("error")) {
                JsonObject errorObj = json.getAsJsonObject("error");
                String rawMsg = errorObj.has("message") ? errorObj.get("message").getAsString() : "";

                return switch (rawMsg) {
                    case "EMAIL_EXISTS" -> "This email address is already registered. Please log in.";
                    case "OPERATION_NOT_ALLOWED" -> "Password sign-in is not enabled. Please enable Email/Password in Firebase Console.";
                    case "TOO_MANY_ATTEMPTS_TRY_LATER" -> "Too many failed attempts. Please try again later.";
                    case "EMAIL_NOT_FOUND" -> "No account found with this email. Please register first.";
                    case "INVALID_PASSWORD" -> "Incorrect password. Please verify and try again.";
                    case "INVALID_LOGIN_CREDENTIALS" -> "Invalid email or password. Please try again.";
                    case "USER_DISABLED" -> "This student account has been disabled.";
                    case "WEAK_PASSWORD : Password should be at least 6 characters" -> "Password is too weak. It must be at least 6 characters.";
                    case "INVALID_EMAIL" -> "The email address is improperly formatted.";
                    default -> defaultMessage + ": " + rawMsg;
                };
            }
        } catch (Exception e) {
            logger.debug("Could not parse JSON error response: {}", responseBody);
        }

        return defaultMessage;
    }
}
