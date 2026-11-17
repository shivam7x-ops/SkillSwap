package com.skillswap.controller;

import com.skillswap.model.DashboardStats;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import com.skillswap.service.AuthService;
import com.skillswap.service.StudentService;
import com.skillswap.view.MainFrame;
import com.skillswap.view.panels.DashboardHomePanel;
import com.skillswap.view.panels.DashboardPanel;
import com.skillswap.view.panels.SidebarPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller mediating between {@link DashboardPanel} and Firebase services.
 *
 * <p>Handles:
 * <ul>
 *   <li>Asynchronous data loading from Firebase Realtime Database</li>
 *   <li>Updating UI state (Loading, Ready, Error, Empty)</li>
 *   <li>Navigation between sub-tabs</li>
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
        // Refresh button click
        dashboardPanel.getHomePanel().setOnRefreshRequested(() -> {
            if (currentStudent != null) {
                loadDashboardData(currentStudent);
            }
        });

        // Logout action from sidebar
        dashboardPanel.getSidebarPanel().setOnLogout(this::handleLogout);
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

        // 2. Set UI to Loading state
        dashboardPanel.getHomePanel().setState(DashboardHomePanel.ViewState.LOADING);

        // 3. Fetch latest metrics and requests from Firebase Realtime Database
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
                        }

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
}
