package com.skillswap.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.skillswap.model.DashboardStats;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service handling student data retrieval and metrics calculation
 * from Firebase Realtime Database.
 */
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private static volatile StudentService instance;
    private final FirebaseService firebaseService;

    public StudentService() {
        this.firebaseService = FirebaseService.getInstance();
    }

    public StudentService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public static StudentService getInstance() {
        if (instance == null) {
            synchronized (StudentService.class) {
                if (instance == null) {
                    instance = new StudentService();
                }
            }
        }
        return instance;
    }

    /**
     * Fetches the complete student profile from Firebase Realtime Database at {@code /users/{uid}}.
     *
     * @param uid student's Firebase Authentication UID
     * @return CompletableFuture with User model
     */
    public CompletableFuture<User> fetchStudentProfile(String uid) {
        if (uid == null || uid.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("UID cannot be null or blank"));
        }

        return firebaseService.getData("users/" + uid)
                .thenApply(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUid() == null) {
                                user.setUid(uid);
                            }
                            if (user.getSkillsToTeach() == null) {
                                user.setSkillsToTeach(new ArrayList<>());
                            }
                            if (user.getSkillsToLearn() == null) {
                                user.setSkillsToLearn(new ArrayList<>());
                            }
                            return user;
                        }
                    }
                    logger.warn("No user record found in Realtime Database for UID: {}", uid);
                    return null;
                });
    }

    /**
     * Fetches all skill exchange requests involving the student (either as sender or receiver)
     * from {@code /requests}.
     *
     * @param uid student's Firebase UID
     * @return CompletableFuture with list of SkillRequest items
     */
    public CompletableFuture<List<SkillRequest>> fetchStudentRequests(String uid) {
        if (uid == null || uid.isBlank()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return firebaseService.getData("requests")
                .thenApply(snapshot -> {
                    List<SkillRequest> userRequests = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            try {
                                SkillRequest req = child.getValue(SkillRequest.class);
                                if (req != null) {
                                    if (req.getRequestId() == null) {
                                        req.setRequestId(child.getKey());
                                    }
                                    if (uid.equals(req.getSenderUid()) || uid.equals(req.getReceiverUid())) {
                                        userRequests.add(req);
                                    }
                                }
                            } catch (Exception e) {
                                logger.debug("Skipping invalid request node: {}", child.getKey());
                            }
                        }
                    }
                    // Sort latest first
                    userRequests.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    return userRequests;
                });
    }

    /**
     * Computes the 4 key dashboard metrics:
     * <ol>
     *   <li>Skills Offered: count of {@code skillsToTeach}</li>
     *   <li>Skills Wanted: count of {@code skillsToLearn}</li>
     *   <li>Active Connections: count of ACCEPTED requests involving this student</li>
     *   <li>Pending Requests: count of PENDING requests awaiting response or sent</li>
     * </ol>
     *
     * @param uid student's Firebase UID
     * @return CompletableFuture with DashboardStats
     */
    public CompletableFuture<DashboardStats> fetchDashboardStats(String uid) {
        CompletableFuture<User> profileFuture = fetchStudentProfile(uid);
        CompletableFuture<List<SkillRequest>> requestsFuture = fetchStudentRequests(uid);

        return profileFuture.thenCombine(requestsFuture, (user, requests) -> {
            int skillsOffered = 0;
            int skillsWanted = 0;

            if (user != null) {
                if (user.getSkillsToTeach() != null) {
                    skillsOffered = user.getSkillsToTeach().size();
                }
                if (user.getSkillsToLearn() != null) {
                    skillsWanted = user.getSkillsToLearn().size();
                }
            }

            int activeConnections = 0;
            int pendingRequests = 0;

            for (SkillRequest req : requests) {
                if (SkillRequest.Status.ACCEPTED.name().equalsIgnoreCase(req.getStatus())) {
                    activeConnections++;
                } else if (SkillRequest.Status.PENDING.name().equalsIgnoreCase(req.getStatus())) {
                    pendingRequests++;
                }
            }

            return new DashboardStats(skillsOffered, skillsWanted, activeConnections, pendingRequests);
        });
    }

    /**
     * Attaches a real-time listener to the student's profile node.
     */
    public void attachProfileListener(String uid, ValueEventListener listener) {
        if (uid != null && listener != null) {
            firebaseService.addValueEventListener("users/" + uid, listener);
        }
    }

    /**
     * Detaches a listener from the student's profile node.
     */
    public void detachProfileListener(String uid, ValueEventListener listener) {
        if (uid != null && listener != null) {
            firebaseService.removeEventListener("users/" + uid, listener);
        }
    }
}
