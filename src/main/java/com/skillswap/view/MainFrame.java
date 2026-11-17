package com.skillswap.view;

import com.skillswap.controller.AuthController;
import com.skillswap.controller.DashboardController;
import com.skillswap.model.User;
import com.skillswap.util.UIConstants;
import com.skillswap.view.panels.DashboardPanel;
import com.skillswap.view.panels.LoginPanel;
import com.skillswap.view.panels.RegisterPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application window.
 *
 * <p>Uses a {@link CardLayout} to switch between screens (Login, Register, Dashboard)
 * smoothly within a single modern frame.
 */
public class MainFrame extends JFrame {

    public static final String CARD_LOGIN     = "LOGIN";
    public static final String CARD_REGISTER  = "REGISTER";
    public static final String CARD_DASHBOARD = "DASHBOARD";

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final DashboardPanel dashboardPanel;

    private final AuthController authController;
    private final DashboardController dashboardController;

    public MainFrame() {
        super("SkillSwap – Student Skill Exchange Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIConstants.FRAME_SIZE);
        setMinimumSize(new Dimension(880, 680));
        setLocationRelativeTo(null); // Centre on screen
        getContentPane().setBackground(UIConstants.BG_PRIMARY);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        // 1. Initialize Views
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();
        dashboardPanel = new DashboardPanel();

        // 2. Register panels into CardLayout
        contentPanel.add(loginPanel, CARD_LOGIN);
        contentPanel.add(registerPanel, CARD_REGISTER);
        contentPanel.add(dashboardPanel, CARD_DASHBOARD);

        // 3. Initialize Controllers
        authController = new AuthController(this, loginPanel, registerPanel);
        dashboardController = new DashboardController(this, dashboardPanel);

        setContentPane(contentPanel);

        // Show Login screen by default
        showCard(CARD_LOGIN);
    }

    /**
     * Switch the visible panel to the given card key.
     */
    public void showCard(String cardKey) {
        cardLayout.show(contentPanel, cardKey);
    }

    /**
     * Automatically opens and populates the Student Dashboard upon successful login.
     *
     * @param student User object returned from authentication
     */
    public void openDashboard(User student) {
        dashboardController.loadDashboardData(student);
        showCard(CARD_DASHBOARD);
    }

    /**
     * Register a new panel under a card key.
     */
    public void addCard(JPanel panel, String cardKey) {
        contentPanel.add(panel, cardKey);
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public RegisterPanel getRegisterPanel() {
        return registerPanel;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public AuthController getAuthController() {
        return authController;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }
}
