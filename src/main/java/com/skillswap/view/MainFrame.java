package com.skillswap.view;

import com.skillswap.controller.AuthController;
import com.skillswap.util.UIConstants;
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
    private final AuthController authController;

    public MainFrame() {
        super("SkillSwap – Student Skill Exchange Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(UIConstants.FRAME_SIZE);
        setMinimumSize(new Dimension(850, 650));
        setLocationRelativeTo(null); // Centre on screen
        getContentPane().setBackground(UIConstants.BG_PRIMARY);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UIConstants.BG_PRIMARY);

        // Initialize Authentication Panels
        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();

        // Register panels into CardLayout
        contentPanel.add(loginPanel, CARD_LOGIN);
        contentPanel.add(registerPanel, CARD_REGISTER);

        // Initialize Controller to wire UI with Services
        authController = new AuthController(this, loginPanel, registerPanel);

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
     * Register a new panel under a card key (e.g. for future Dashboard integration).
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

    public AuthController getAuthController() {
        return authController;
    }
}
