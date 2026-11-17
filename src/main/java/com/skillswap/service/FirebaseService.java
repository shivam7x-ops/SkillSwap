package com.skillswap.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service class responsible for Firebase Realtime Database operations.
 *
 * <p>Provides an abstraction layer over the Firebase Realtime Database SDK:
 * <ul>
 *   <li>Reference navigation and key generation</li>
 *   <li>Asynchronous data reading, writing, updating, and deletion with {@link CompletableFuture}</li>
 *   <li>Real-time event listener management</li>
 *   <li>Connection status monitoring via Firebase internal {@code /.info/connected} path</li>
 *   <li>Consistent database error conversion to {@link FirebaseDatabaseException}</li>
 * </ul>
 *
 * <p>Notice: This service does not create or enforce predefined database structures
 * or sample records. It operates purely as a data-access and connection handler.
 */
public class FirebaseService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    private static volatile FirebaseService instance;
    private final FirebaseDatabase database;

    /**
     * Initializes the FirebaseService using the configured FirebaseDatabase instance.
     */
    public FirebaseService() {
        this.database = FirebaseConfig.getDatabase();
    }

    /**
     * Dependency injection constructor.
     */
    public FirebaseService(FirebaseDatabase database) {
        this.database = Objects.requireNonNull(database, "FirebaseDatabase instance cannot be null");
    }

    /**
     * Thread-safe singleton accessor.
     */
    public static FirebaseService getInstance() {
        if (instance == null) {
            synchronized (FirebaseService.class) {
                if (instance == null) {
                    instance = new FirebaseService();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the underlying {@link FirebaseDatabase} instance.
     */
    public FirebaseDatabase getDatabase() {
        return database;
    }

    /**
     * Returns a {@link DatabaseReference} pointing to the root of the database.
     */
    public DatabaseReference getRootReference() {
        return database.getReference();
    }

    /**
     * Returns a {@link DatabaseReference} pointing to the specified child path.
     *
     * @param path relative path (e.g. "users", "requests")
     * @return DatabaseReference at the path
     */
    public DatabaseReference getReference(String path) {
        if (path == null || path.isBlank() || "/".equals(path.trim())) {
            return getRootReference();
        }
        return database.getReference(path.trim());
    }

    /**
     * Generates a new unique push ID under the specified path.
     *
     * @param path target path
     * @return unique generated key
     */
    public String pushKey(String path) {
        DatabaseReference childRef = getReference(path).push();
        return childRef.getKey();
    }

    /**
     * Asynchronously writes or overwrites data at the specified path.
     *
     * @param path  target location
     * @param value data to store (POJO, Map, List, or primitive)
     * @return CompletableFuture completing when the write succeeds or failing with {@link FirebaseDatabaseException}
     */
    public CompletableFuture<Void> setValue(String path, Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference ref = getReference(path);

        ref.setValue(value, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                logger.error("Failed to write data at path '{}': {} (code: {})",
                        path, databaseError.getMessage(), databaseError.getCode());
                future.completeExceptionally(
                        new FirebaseDatabaseException("Failed to set value at: " + path, databaseError));
            } else {
                logger.debug("Successfully saved data at path: {}", path);
                future.complete(null);
            }
        });

        return future;
    }

    /**
     * Asynchronously updates specific children at the specified path without overwriting other nodes.
     *
     * @param path   target location
     * @param update map of child paths to values
     * @return CompletableFuture completing when update succeeds
     */
    public CompletableFuture<Void> updateChildren(String path, Map<String, Object> update) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference ref = getReference(path);

        ref.updateChildren(update, (databaseError, databaseReference) -> {
            if (databaseError != null) {
                logger.error("Failed to update children at path '{}': {} (code: {})",
                        path, databaseError.getMessage(), databaseError.getCode());
                future.completeExceptionally(
                        new FirebaseDatabaseException("Failed to update children at: " + path, databaseError));
            } else {
                logger.debug("Successfully updated children at path: {}", path);
                future.complete(null);
            }
        });

        return future;
    }

    /**
     * Asynchronously reads a single snapshot of data at the specified path.
     *
     * @param path target path to read
     * @return CompletableFuture containing the {@link DataSnapshot}
     */
    public CompletableFuture<DataSnapshot> getData(String path) {
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();
        DatabaseReference ref = getReference(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                future.complete(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.error("Read operation cancelled for path '{}': {} (code: {})",
                        path, error.getMessage(), error.getCode());
                future.completeExceptionally(
                        new FirebaseDatabaseException("Read cancelled at: " + path, error));
            }
        });

        return future;
    }

    /**
     * Asynchronously deletes the data at the specified path.
     *
     * @param path target location to remove
     * @return CompletableFuture completing upon deletion
     */
    public CompletableFuture<Void> removeValue(String path) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        DatabaseReference ref = getReference(path);

        ref.removeValue((databaseError, databaseReference) -> {
            if (databaseError != null) {
                logger.error("Failed to remove value at path '{}': {} (code: {})",
                        path, databaseError.getMessage(), databaseError.getCode());
                future.completeExceptionally(
                        new FirebaseDatabaseException("Failed to remove value at: " + path, databaseError));
            } else {
                logger.debug("Successfully deleted value at path: {}", path);
                future.complete(null);
            }
        });

        return future;
    }

    /**
     * Registers a continuous {@link ValueEventListener} on the specified path.
     *
     * @param path     target path
     * @param listener event listener callback
     */
    public void addValueEventListener(String path, ValueEventListener listener) {
        Objects.requireNonNull(listener, "ValueEventListener cannot be null");
        getReference(path).addValueEventListener(listener);
    }

    /**
     * Unregisters a {@link ValueEventListener} from the specified path.
     *
     * @param path     target path
     * @param listener listener to remove
     */
    public void removeEventListener(String path, ValueEventListener listener) {
        if (listener != null) {
            getReference(path).removeEventListener(listener);
        }
    }

    /**
     * Subscribes a listener to monitor Realtime Database connection status changes.
     * Firebase Realtime Database maintains an internal status at {@code .info/connected}.
     *
     * @param connectionListener consumer receiving true when online, false when disconnected
     * @return ValueEventListener instance that can be removed later if needed
     */
    public ValueEventListener addConnectionListener(Consumer<Boolean> connectionListener) {
        Objects.requireNonNull(connectionListener, "Connection listener cannot be null");
        DatabaseReference connectedRef = getReference(".info/connected");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                boolean isConnected = Boolean.TRUE.equals(connected);
                logger.info("Firebase Realtime Database connection state changed: {}", isConnected ? "CONNECTED" : "DISCONNECTED");
                connectionListener.accept(isConnected);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.warn("Firebase connection listener cancelled: {}", error.getMessage());
                connectionListener.accept(false);
            }
        };

        connectedRef.addValueEventListener(listener);
        return listener;
    }

    /**
     * Tests connectivity to Firebase Realtime Database without creating database structures
     * or writing sample records.
     *
     * <p>Uses the Firebase internal read-only path {@code .info/connected} with a timeout.
     *
     * @param timeoutSeconds maximum seconds to wait for connection confirmation
     * @return true if successfully connected, false if timed out or failed
     */
    public boolean testConnection(long timeoutSeconds) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        DatabaseReference connectedRef = getReference(".info/connected");

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean connected = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(connected)) {
                    future.complete(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                logger.error("Connection test cancelled: {}", error.getMessage());
                future.completeExceptionally(new FirebaseDatabaseException("Connection test cancelled", error));
            }
        };

        connectedRef.addValueEventListener(listener);

        try {
            return future.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            logger.warn("Connection test timed out after {} seconds (waiting for online state)", timeoutSeconds);
            return false;
        } catch (Exception e) {
            logger.error("Connection test error: {}", e.getMessage());
            return false;
        } finally {
            connectedRef.removeEventListener(listener);
        }
    }
}
