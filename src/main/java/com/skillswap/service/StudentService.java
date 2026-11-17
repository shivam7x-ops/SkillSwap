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
     * Updates student profile fields (name and college) in Firebase Realtime Database.
     *
     * @param uid        student's Firebase UID
     * @param newName    updated full name
     * @param newCollege updated college
     * @return CompletableFuture completing when write succeeds
     */
    public CompletableFuture<Void> updateStudentProfile(String uid, String newName, String newCollege) {
        if (uid == null || uid.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("UID cannot be null or blank"));
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("name", newName != null ? newName.trim() : "");
        updates.put("college", newCollege != null ? newCollege.trim() : "");

        logger.info("Updating profile in Firebase for UID: {}", uid);
        return firebaseService.updateChildren("users/" + uid, updates);
    }

    /**
     * Updates the full list of skills the student can teach.
     *
     * @param uid    student's Firebase UID
     * @param skills new list of skills to teach
     * @return CompletableFuture completing when write succeeds
     */
    public CompletableFuture<Void> updateSkillsToTeach(String uid, List<String> skills) {
        if (uid == null || uid.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("UID cannot be null or blank"));
        }
        List<String> cleanList = cleanSkillsList(skills);
        logger.info("Saving {} skillsToTeach for UID: {}", cleanList.size(), uid);
        return firebaseService.setValue("users/" + uid + "/skillsToTeach", cleanList);
    }

    /**
     * Updates the full list of skills the student wants to learn.
     *
     * @param uid    student's Firebase UID
     * @param skills new list of skills to learn
     * @return CompletableFuture completing when write succeeds
     */
    public CompletableFuture<Void> updateSkillsToLearn(String uid, List<String> skills) {
        if (uid == null || uid.isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("UID cannot be null or blank"));
        }
        List<String> cleanList = cleanSkillsList(skills);
        logger.info("Saving {} skillsToLearn for UID: {}", cleanList.size(), uid);
        return firebaseService.setValue("users/" + uid + "/skillsToLearn", cleanList);
    }

    /**
     * Adds a skill to teach, validating for non-blank and duplicates.
     */
    public CompletableFuture<List<String>> addSkillToTeach(String uid, String skill, List<String> currentList) {
        String validationErr = validateNewSkill(skill, currentList);
        if (validationErr != null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(validationErr));
        }

        List<String> updated = new ArrayList<>(currentList != null ? currentList : Collections.emptyList());
        updated.add(skill.trim());

        return updateSkillsToTeach(uid, updated).thenApply(v -> updated);
    }

    /**
     * Edits an existing skill in skillsToTeach.
     */
    public CompletableFuture<List<String>> editSkillToTeach(String uid, String oldSkill, String newSkill, List<String> currentList) {
        if (oldSkill == null || newSkill == null || newSkill.trim().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Skill name cannot be empty."));
        }
        if (oldSkill.trim().equalsIgnoreCase(newSkill.trim())) {
            return CompletableFuture.completedFuture(currentList); // No change needed
        }

        List<String> otherSkills = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (!s.equalsIgnoreCase(oldSkill.trim())) {
                    otherSkills.add(s);
                }
            }
        }

        String validationErr = validateNewSkill(newSkill, otherSkills);
        if (validationErr != null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(validationErr));
        }

        List<String> updated = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (s.equalsIgnoreCase(oldSkill.trim())) {
                    updated.add(newSkill.trim());
                } else {
                    updated.add(s);
                }
            }
        }

        return updateSkillsToTeach(uid, updated).thenApply(v -> updated);
    }

    /**
     * Deletes a skill from skillsToTeach.
     */
    public CompletableFuture<List<String>> deleteSkillToTeach(String uid, String skillToDelete, List<String> currentList) {
        List<String> updated = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (!s.equalsIgnoreCase(skillToDelete.trim())) {
                    updated.add(s);
                }
            }
        }
        return updateSkillsToTeach(uid, updated).thenApply(v -> updated);
    }

    /**
     * Adds a skill to learn (learning interest), validating for non-blank and duplicates.
     */
    public CompletableFuture<List<String>> addSkillToLearn(String uid, String skill, List<String> currentList) {
        String validationErr = validateNewSkill(skill, currentList);
        if (validationErr != null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(validationErr));
        }

        List<String> updated = new ArrayList<>(currentList != null ? currentList : Collections.emptyList());
        updated.add(skill.trim());

        return updateSkillsToLearn(uid, updated).thenApply(v -> updated);
    }

    /**
     * Edits an existing skill in skillsToLearn.
     */
    public CompletableFuture<List<String>> editSkillToLearn(String uid, String oldSkill, String newSkill, List<String> currentList) {
        if (oldSkill == null || newSkill == null || newSkill.trim().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Skill name cannot be empty."));
        }
        if (oldSkill.trim().equalsIgnoreCase(newSkill.trim())) {
            return CompletableFuture.completedFuture(currentList); // No change
        }

        List<String> otherSkills = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (!s.equalsIgnoreCase(oldSkill.trim())) {
                    otherSkills.add(s);
                }
            }
        }

        String validationErr = validateNewSkill(newSkill, otherSkills);
        if (validationErr != null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException(validationErr));
        }

        List<String> updated = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (s.equalsIgnoreCase(oldSkill.trim())) {
                    updated.add(newSkill.trim());
                } else {
                    updated.add(s);
                }
            }
        }

        return updateSkillsToLearn(uid, updated).thenApply(v -> updated);
    }

    /**
     * Deletes a skill from skillsToLearn.
     */
    public CompletableFuture<List<String>> deleteSkillToLearn(String uid, String skillToDelete, List<String> currentList) {
        List<String> updated = new ArrayList<>();
        if (currentList != null) {
            for (String s : currentList) {
                if (!s.equalsIgnoreCase(skillToDelete.trim())) {
                    updated.add(s);
                }
            }
        }
        return updateSkillsToLearn(uid, updated).thenApply(v -> updated);
    }

    public String validateNewSkill(String skill, List<String> existingList) {
        if (skill == null || skill.trim().isBlank()) {
            return "Skill name cannot be empty.";
        }
        String clean = skill.trim();
        if (clean.length() < 2) {
            return "Skill name must be at least 2 characters.";
        }
        if (existingList != null) {
            for (String existing : existingList) {
                if (existing != null && existing.trim().equalsIgnoreCase(clean)) {
                    return "You have already added this skill.";
                }
            }
        }
        return null;
    }

    public List<String> cleanSkillsList(List<String> list) {
        if (list == null) return Collections.emptyList();
        List<String> cleaned = new ArrayList<>();
        java.util.Set<String> seen = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String s : list) {
            if (s != null && !s.trim().isBlank()) {
                String clean = s.trim();
                if (seen.add(clean)) {
                    cleaned.add(clean);
                }
            }
        }
        Collections.sort(cleaned);
        return cleaned;
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
