package com.skillswap.service;

import com.google.firebase.database.DatabaseError;

/**
 * Custom runtime exception wrapping Firebase Database errors.
 */
public class FirebaseDatabaseException extends RuntimeException {

    private final int errorCode;
    private final String errorDetails;

    public FirebaseDatabaseException(String message) {
        super(message);
        this.errorCode = 0;
        this.errorDetails = null;
    }

    public FirebaseDatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
        this.errorDetails = null;
    }

    public FirebaseDatabaseException(DatabaseError error) {
        super(error != null ? error.getMessage() : "Unknown Firebase Database error");
        this.errorCode = error != null ? error.getCode() : 0;
        this.errorDetails = error != null ? error.getDetails() : null;
    }

    public FirebaseDatabaseException(String message, DatabaseError error) {
        super(message + (error != null ? " (" + error.getMessage() + ")" : ""));
        this.errorCode = error != null ? error.getCode() : 0;
        this.errorDetails = error != null ? error.getDetails() : null;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    @Override
    public String toString() {
        return "FirebaseDatabaseException{" +
                "message='" + getMessage() + '\'' +
                ", errorCode=" + errorCode +
                ", errorDetails='" + errorDetails + '\'' +
                '}';
    }
}
