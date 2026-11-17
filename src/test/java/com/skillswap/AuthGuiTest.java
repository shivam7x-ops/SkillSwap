package com.skillswap;

import com.skillswap.util.ValidationUtil;
import com.skillswap.view.components.StyledButton;
import com.skillswap.view.components.StyledPasswordField;
import com.skillswap.view.components.StyledTextField;
import com.skillswap.view.panels.LoginPanel;
import com.skillswap.view.panels.RegisterPanel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying Login & Registration validation rules,
 * reusable components, and view panel behavior.
 */
public class AuthGuiTest {

    @Test
    @DisplayName("Input Validation: Email Format")
    public void testEmailValidation() {
        assertNull(ValidationUtil.validateEmail("student@stanford.edu"));
        assertNull(ValidationUtil.validateEmail("john.doe@mit.edu"));
        assertNotNull(ValidationUtil.validateEmail(null));
        assertNotNull(ValidationUtil.validateEmail(""));
        assertNotNull(ValidationUtil.validateEmail("not-an-email"));
        assertNotNull(ValidationUtil.validateEmail("missing@domain"));
    }

    @Test
    @DisplayName("Input Validation: Password Rules & Confirmation")
    public void testPasswordValidation() {
        assertNull(ValidationUtil.validatePassword("secret123"));
        assertNotNull(ValidationUtil.validatePassword("12345")); // too short (< 6 chars)
        assertNotNull(ValidationUtil.validatePassword(null));
        assertNotNull(ValidationUtil.validatePassword(""));

        // Password matching
        assertNull(ValidationUtil.validatePasswordConfirmation("pass123", "pass123"));
        assertNotNull(ValidationUtil.validatePasswordConfirmation("pass123", "different"));
        assertNotNull(ValidationUtil.validatePasswordConfirmation("pass123", ""));
    }

    @Test
    @DisplayName("Input Validation: Full Registration & Login Rules")
    public void testFullFormValidation() {
        // Valid registration
        String error = ValidationUtil.validateRegistration(
                "Jane Doe", "jane@college.edu", "pass123", "pass123", "Harvard");
        assertNull(error);

        // Invalid registration: mismatched passwords
        error = ValidationUtil.validateRegistration(
                "Jane Doe", "jane@college.edu", "pass123", "mismatch", "Harvard");
        assertEquals("Passwords do not match.", error);

        // Invalid registration: empty name
        error = ValidationUtil.validateRegistration(
                "", "jane@college.edu", "pass123", "pass123", "Harvard");
        assertEquals("Name cannot be empty.", error);

        // Valid login
        assertNull(ValidationUtil.validateLogin("student@college.edu", "password123"));
        // Invalid login: empty password
        assertEquals("Password cannot be empty.", ValidationUtil.validateLogin("student@college.edu", ""));
    }

    @Test
    @DisplayName("Reusable Component: StyledPasswordField Show/Hide Toggle")
    public void testPasswordShowHideToggle() {
        StyledPasswordField pwd = new StyledPasswordField("Enter password");
        pwd.setText("MySecretPassword");

        assertEquals("MySecretPassword", pwd.getPasswordString());

        // Default: masked with bullet
        assertEquals('\u2022', pwd.getPasswordField().getEchoChar());

        // Find toggle button and click it to reveal
        JButton toggleBtn = null;
        for (Component comp : pwd.getComponents()) {
            if (comp instanceof JButton btn) {
                toggleBtn = btn;
                break;
            }
        }
        assertNotNull(toggleBtn, "Toggle button should exist in StyledPasswordField");

        // Click toggle button -> reveal
        toggleBtn.doClick();
        assertEquals((char) 0, pwd.getPasswordField().getEchoChar(), "Password should be visible (echoChar == 0)");

        // Click toggle button again -> mask
        toggleBtn.doClick();
        assertEquals('\u2022', pwd.getPasswordField().getEchoChar(), "Password should be masked again");
    }

    @Test
    @DisplayName("View Panels: LoginPanel & RegisterPanel Component State")
    public void testViewPanelsInitialization() {
        LoginPanel loginPanel = new LoginPanel();
        assertNotNull(loginPanel);
        assertEquals("", loginPanel.getEmail());
        assertEquals("", loginPanel.getPassword());

        loginPanel.setErrorMessage("Invalid credentials");
        loginPanel.clearMessages();

        RegisterPanel registerPanel = new RegisterPanel();
        assertNotNull(registerPanel);
        assertEquals("", registerPanel.getFullName());
        assertEquals("", registerPanel.getEmail());
        assertEquals("", registerPanel.getCollege());
        assertEquals("", registerPanel.getPassword());
        assertEquals("", registerPanel.getConfirmPassword());

        registerPanel.setErrorMessage("Passwords do not match");
        registerPanel.clearMessages();
    }

    @Test
    @DisplayName("Navigation: CardLayout Screen Switching between Login and Register")
    public void testCardLayoutNavigation() {
        CardLayout cardLayout = new CardLayout();
        JPanel container = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel();
        RegisterPanel registerPanel = new RegisterPanel();

        container.add(loginPanel, "LOGIN");
        container.add(registerPanel, "REGISTER");

        assertDoesNotThrow(() -> cardLayout.show(container, "REGISTER"));
        assertDoesNotThrow(() -> cardLayout.show(container, "LOGIN"));
    }
}
