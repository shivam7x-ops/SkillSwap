package com.skillswap.model;

/**
 * Result object returned by authentication operations.
 */
public class AuthResult {

    private final boolean success;
    private final String message;
    private final User user;
    private final String idToken;

    private AuthResult(boolean success, String message, User user, String idToken) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.idToken = idToken;
    }

    public static AuthResult success(String message, User user, String idToken) {
        return new AuthResult(true, message, user, idToken);
    }

    public static AuthResult failure(String errorMessage) {
        return new AuthResult(false, errorMessage, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public String getIdToken() {
        return idToken;
    }
}
