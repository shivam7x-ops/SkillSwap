package com.skillswap.controller;

import com.skillswap.model.DashboardStats;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import com.skillswap.service.AuthService;
import com.skillswap.service.StudentService;
import com.skillswap.util.ValidationUtil;
import com.skillswap.view.MainFrame;
import com.skillswap.view.panels.DashboardHomePanel;
import com.skillswap.view.panels.DashboardPanel;
import com.skillswap.view.panels.ProfilePanel;
import com.skillswap.view.panels.SidebarPanel;
import com.skillswap.view.panels.SkillsManagementPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * Controller mediating between {@link DashboardPanel} and Firebase services.
 *
 * <p>Handles:
 * <ul>
 *   <li>Asynchronous data loading from Firebase Realtime Database</li>
 *   <li>Updating UI state (Loading, Ready, Error, Empty)</li>
 *   <li>Navigation between sub-tabs</li>
 *   <li>Profile editing and saving to Firebase Realtime Database</li>
 *   <li>Skills management (Teach & Learn: Add, Edit, Delete, Duplicate Prevention)</li>
 *   <li>Student logout via Firebase Authentication</li>
 * </ul>
 */
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final MainFrame mainFrame;
    private final DashboardPanel dashboardPanel;
    private final StudentService studentService;
    private final AuthService authService;

    private User currentStudent;
    private DashboardStats latestStats;

    public DashboardController(MainFrame mainFrame, DashboardPanel dashboardPanel) {
        this(mainFrame, dashboardPanel, StudentService.getInstance(), AuthService.getInstance());
    }

    public DashboardController(MainFrame mainFrame, DashboardPanel dashboardPanel,
                               StudentService studentService, AuthService authService) {
        this.mainFrame = mainFrame;
        this.dashboardPanel = dashboardPanel;
        this.studentService = studentService;
        this.authService = authService;

        initListeners();
    }

    private void initListeners() {
        // Refresh button click on Home
        dashboardPanel.getHomePanel().setOnRefreshRequested(() -> {
            if (currentStudent != null) {
                loadDashboardData(currentStudent);
            }
        });

        // Logout action from sidebar
        dashboardPanel.getSidebarPanel().setOnLogout(this::handleLogout);

        // Profile panel actions
        ProfilePanel profilePanel = dashboardPanel.getProfilePanel();
        profilePanel.setOnSave(this::handleSaveProfile);
        profilePanel.setOnCancel(() -> {
            profilePanel.resetFields();
            profilePanel.clearMessage();
        });

        // Skills management panel actions
        SkillsManagementPanel skillsPanel = dashboardPanel.getSkillsPanel();
        skillsPanel.setOnAddTeach(this::handleAddTeachSkill);
        skillsPanel.setOnEditTeach(this::handleEditTeachSkill);
        skillsPanel.setOnDeleteTeach(this::handleDeleteTeachSkill);

        skillsPanel.setOnAddLearn(this::handleAddLearnSkill);
        skillsPanel.setOnEditLearn(this::handleEditLearnSkill);
        skillsPanel.setOnDeleteLearn(this::handleDeleteLearnSkill);
    }

    /**
     * Initializes and loads the dashboard for the logged-in student.
     *
     * @param student User object from authentication
     */
    public void loadDashboardData(User student) {
        if (student == null) {
            dashboardPanel.getHomePanel().setError("No active student session found.");
            return;
        }

        this.currentStudent = student;
        String uid = student.getUid();

        // 1. Immediately reflect initial student info in UI
        dashboardPanel.getSidebarPanel().setStudent(student);
        dashboardPanel.getSidebarPanel().selectTab(SidebarPanel.TAB_DASHBOARD);
        dashboardPanel.getHomePanel().setStudentInfo(student);
        dashboardPanel.getProfilePanel().setStudent(student);
        dashboardPanel.getSkillsPanel().setTeachSkills(student.getSkillsToTeach());
        dashboardPanel.getSkillsPanel().setLearnSkills(student.getSkillsToLearn());

        // 2. Set UI to Loading state
        dashboardPanel.getHomePanel().setState(DashboardHomePanel.ViewState.LOADING);

        // 3. Fetch latest metrics, profile, and requests from Firebase Realtime Database
        CompletableFuture<User> profileFuture = studentService.fetchStudentProfile(uid);
        CompletableFuture<DashboardStats> statsFuture = studentService.fetchDashboardStats(uid);
        CompletableFuture<List<SkillRequest>> requestsFuture = studentService.fetchStudentRequests(uid);

        CompletableFuture.allOf(profileFuture, statsFuture, requestsFuture)
                .thenAccept(v -> {
                    User updatedUser = profileFuture.join();
                    DashboardStats stats = statsFuture.join();
                    List<SkillRequest> requests = requestsFuture.join();

                    SwingUtilities.invokeLater(() -> {
                        if (updatedUser != null) {
                            this.currentStudent = updatedUser;
                            dashboardPanel.getSidebarPanel().setStudent(updatedUser);
                            dashboardPanel.getHomePanel().setStudentInfo(updatedUser);
                            dashboardPanel.getProfilePanel().setStudent(updatedUser);
                            dashboardPanel.getSkillsPanel().setTeachSkills(updatedUser.getSkillsToTeach());
                            dashboardPanel.getSkillsPanel().setLearnSkills(updatedUser.getSkillsToLearn());
                        }

                        this.latestStats = stats;
                        dashboardPanel.getHomePanel().setStats(stats);
                        dashboardPanel.getHomePanel().setRecentRequests(requests, uid);
                        dashboardPanel.getHomePanel().setState(DashboardHomePanel.ViewState.READY);

                        logger.info("Dashboard loaded successfully for UID: {}", uid);
                    });
                })
                .exceptionally(ex -> {
                    logger.error("Failed to load dashboard data for UID: {}", uid, ex);
                    SwingUtilities.invokeLater(() -> {
                        dashboardPanel.getHomePanel().setError("Could not load data from Firebase: " + ex.getMessage());
                    });
                    return null;
                });
    }

    // ── Profile Actions ──────────────────────────────────────────────────

    private void handleSaveProfile(ProfilePanel panel) {
        if (currentStudent == null) return;

        String newName = panel.getFullName();
        String newCollege = panel.getCollege();

        String nameErr = ValidationUtil.validateName(newName);
        if (nameErr != null) {
            panel.setErrorMessage(nameErr);
            return;
        }

        String collegeErr = ValidationUtil.validateCollege(newCollege);
        if (collegeErr != null) {
            panel.setErrorMessage(collegeErr);
            return;
        }

        panel.setLoading(true);
        panel.clearMessage();

        studentService.updateStudentProfile(currentStudent.getUid(), newName, newCollege)
                .thenAccept(v -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setName(newName);
                    currentStudent.setCollege(newCollege);

                    // Synchronize sidebar, home banner, and profile view
                    dashboardPanel.getSidebarPanel().setStudent(currentStudent);
                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    panel.setStudent(currentStudent);
                    panel.setLoading(false);
                    panel.setSuccessMessage("Profile updated successfully!");

                    logger.info("Profile successfully updated for UID: {}", currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    logger.error("Failed to update profile for UID: {}", currentStudent.getUid(), ex);
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage("Failed to update profile: " + extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    // ── Skills I Can Teach Actions ───────────────────────────────────────

    private void handleAddTeachSkill(String skill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.addSkillToTeach(currentStudent.getUid(), skill, currentStudent.getSkillsToTeach())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToTeach(updatedList);
                    panel.setTeachSkills(updatedList);
                    panel.clearTeachInput();
                    panel.setLoading(false);
                    panel.setSuccessMessage("Added teaching skill: \"" + skill.trim() + "\"");

                    // Refresh dashboard home chips and summary card
                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Added teach skill '{}' for UID: {}", skill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void handleEditTeachSkill(String oldSkill, String newSkill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.editSkillToTeach(currentStudent.getUid(), oldSkill, newSkill, currentStudent.getSkillsToTeach())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToTeach(updatedList);
                    panel.setTeachSkills(updatedList);
                    panel.setLoading(false);
                    panel.setSuccessMessage("Updated skill: \"" + oldSkill + "\" → \"" + newSkill.trim() + "\"");

                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Edited teach skill '{}' to '{}' for UID: {}", oldSkill, newSkill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void handleDeleteTeachSkill(String skill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.deleteSkillToTeach(currentStudent.getUid(), skill, currentStudent.getSkillsToTeach())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToTeach(updatedList);
                    panel.setTeachSkills(updatedList);
                    panel.setLoading(false);
                    panel.setSuccessMessage("Removed teaching skill: \"" + skill + "\"");

                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Deleted teach skill '{}' for UID: {}", skill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    // ── Skills I Want to Learn Actions ───────────────────────────────────

    private void handleAddLearnSkill(String skill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.addSkillToLearn(currentStudent.getUid(), skill, currentStudent.getSkillsToLearn())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToLearn(updatedList);
                    panel.setLearnSkills(updatedList);
                    panel.clearLearnInput();
                    panel.setLoading(false);
                    panel.setSuccessMessage("Added learning goal: \"" + skill.trim() + "\"");

                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Added learn skill '{}' for UID: {}", skill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void handleEditLearnSkill(String oldSkill, String newSkill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.editSkillToLearn(currentStudent.getUid(), oldSkill, newSkill, currentStudent.getSkillsToLearn())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToLearn(updatedList);
                    panel.setLearnSkills(updatedList);
                    panel.setLoading(false);
                    panel.setSuccessMessage("Updated interest: \"" + oldSkill + "\" → \"" + newSkill.trim() + "\"");

                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Edited learn skill '{}' to '{}' for UID: {}", oldSkill, newSkill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    private void handleDeleteLearnSkill(String skill) {
        if (currentStudent == null) return;

        SkillsManagementPanel panel = dashboardPanel.getSkillsPanel();
        panel.setLoading(true);
        panel.clearMessage();

        studentService.deleteSkillToLearn(currentStudent.getUid(), skill, currentStudent.getSkillsToLearn())
                .thenAccept(updatedList -> SwingUtilities.invokeLater(() -> {
                    currentStudent.setSkillsToLearn(updatedList);
                    panel.setLearnSkills(updatedList);
                    panel.setLoading(false);
                    panel.setSuccessMessage("Removed learning goal: \"" + skill + "\"");

                    dashboardPanel.getHomePanel().setStudentInfo(currentStudent);
                    updateSummaryStats();

                    logger.info("Deleted learn skill '{}' for UID: {}", skill, currentStudent.getUid());
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        panel.setLoading(false);
                        panel.setErrorMessage(extractCleanErrorMessage(ex));
                    });
                    return null;
                });
    }

    /**
     * Synchronizes dashboard summary cards count with current student skill counts.
     */
    private void updateSummaryStats() {
        if (currentStudent == null) return;

        int teachCount = currentStudent.getSkillsToTeach() != null ? currentStudent.getSkillsToTeach().size() : 0;
        int learnCount = currentStudent.getSkillsToLearn() != null ? currentStudent.getSkillsToLearn().size() : 0;
        int activeConn = latestStats != null ? latestStats.getActiveConnectionsCount() : 0;
        int pendingReq = latestStats != null ? latestStats.getPendingRequestsCount() : 0;

        latestStats = new DashboardStats(teachCount, learnCount, activeConn, pendingReq);
        dashboardPanel.getHomePanel().setStats(latestStats);
    }

    /**
     * Unwraps nested async exceptions into clean readable error messages.
     */
    private String extractCleanErrorMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null && (cause instanceof CompletionException || cause instanceof ExecutionException)) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : "An unexpected error occurred.";
    }

    /**
     * Handles student logout via Firebase Authentication and returns to Login.
     */
    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to log out of SkillSwap?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            currentStudent = null;
            latestStats = null;

            // Reset views and return to Login
            mainFrame.getLoginPanel().clearFields();
            mainFrame.getLoginPanel().setSuccessMessage("You have been signed out.");
            mainFrame.showCard(MainFrame.CARD_LOGIN);

            logger.info("Student logged out successfully.");
        }
    }

    public User getCurrentStudent() {
        return currentStudent;
    }

    public DashboardStats getLatestStats() {
        return latestStats;
    }
}
