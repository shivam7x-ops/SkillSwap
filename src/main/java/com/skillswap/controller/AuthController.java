package com.skillswap.controller;

import com.skillswap.model.AuthResult;
import com.skillswap.service.AuthService;
import com.skillswap.util.ValidationUtil;
import com.skillswap.view.MainFrame;
import com.skillswap.view.panels.LoginPanel;
import com.skillswap.view.panels.RegisterPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Controller mediating between Authentication views ({@link LoginPanel}, {@link RegisterPanel})
 * and the {@link AuthService}.
 *
 * <p>Enforces input validation and coordinates asynchronous network calls without
 * blocking the Swing Event Dispatch Thread (EDT).
 */
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final MainFrame mainFrame;
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private final AuthService authService;

    public AuthController(MainFrame mainFrame, LoginPanel loginPanel, RegisterPanel registerPanel) {
        this(mainFrame, loginPanel, registerPanel, AuthService.getInstance());
    }

    public AuthController(MainFrame mainFrame, LoginPanel loginPanel, RegisterPanel registerPanel, AuthService authService) {
        this.mainFrame = mainFrame;
        this.loginPanel = loginPanel;
        this.registerPanel = registerPanel;
        this.authService = authService;

        initListeners();
    }

    private void initListeners() {
        // Login View Listeners
        loginPanel.setLoginAction(e -> handleLogin());
        loginPanel.setNavigateToRegisterAction(e -> {
            registerPanel.clearFields();
            loginPanel.clearMessages();
            mainFrame.showCard(MainFrame.CARD_REGISTER);
        });

        // Register View Listeners
        registerPanel.setRegisterAction(e -> handleRegister());
        registerPanel.setNavigateToLoginAction(e -> {
            loginPanel.clearMessages();
            registerPanel.clearMessages();
            mainFrame.showCard(MainFrame.CARD_LOGIN);
        });
    }

    /**
     * Handles student login submission.
     */
    private void handleLogin() {
        String email = loginPanel.getEmail();
        String password = loginPanel.getPassword();

        // 1. Client-side Validation
        String validationError = ValidationUtil.validateLogin(email, password);
        if (validationError != null) {
            loginPanel.setErrorMessage(validationError);
            return;
        }

        loginPanel.clearMessages();
        loginPanel.setLoading(true);

        // 2. Asynchronous Authentication Call
        authService.login(email, password)
                .thenAccept(authResult -> SwingUtilities.invokeLater(() -> {
                    loginPanel.setLoading(false);

                    if (authResult.isSuccess()) {
                        logger.info("Login successful for user: {}", authResult.getUser().getEmail());
                        loginPanel.clearMessages();
                        // Automatically open the Student Dashboard without temporary popup
                        mainFrame.openDashboard(authResult.getUser());
                    } else {
                        loginPanel.setErrorMessage(authResult.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        loginPanel.setLoading(false);
                        loginPanel.setErrorMessage("Unexpected authentication error: " + ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Handles student registration submission.
     */
    private void handleRegister() {
        String name = registerPanel.getFullName();
        String email = registerPanel.getEmail();
        String college = registerPanel.getCollege();
        String password = registerPanel.getPassword();
        String confirmPassword = registerPanel.getConfirmPassword();

        // 1. Client-side Validation
        String validationError = ValidationUtil.validateRegistration(name, email, password, confirmPassword, college);
        if (validationError != null) {
            registerPanel.setErrorMessage(validationError);
            return;
        }

        registerPanel.clearMessages();
        registerPanel.setLoading(true);

        // 2. Asynchronous Registration Call
        authService.register(name, email, password, college)
                .thenAccept(authResult -> SwingUtilities.invokeLater(() -> {
                    registerPanel.setLoading(false);

                    if (authResult.isSuccess()) {
                        logger.info("Registration successful for user: {}", email);
                        JOptionPane.showMessageDialog(
                                mainFrame,
                                "Account created successfully for " + name + "!\n\n"
                                        + "You can now log in using your email and password.",
                                "SkillSwap – Registration Complete",
                                JOptionPane.INFORMATION_MESSAGE);

                        // Navigate to Login screen and pre-fill email
                        mainFrame.showCard(MainFrame.CARD_LOGIN);
                        loginPanel.clearFields();
                        loginPanel.setSuccessMessage("Account created! Please sign in.");
                    } else {
                        registerPanel.setErrorMessage(authResult.getMessage());
                    }
                }))
                .exceptionally(ex -> {
                    SwingUtilities.invokeLater(() -> {
                        registerPanel.setLoading(false);
                        registerPanel.setErrorMessage("Unexpected registration error: " + ex.getMessage());
                    });
                    return null;
                });
    }
}
