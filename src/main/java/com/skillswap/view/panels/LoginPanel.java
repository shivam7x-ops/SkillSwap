package com.skillswap.view.panels;

import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;
import com.skillswap.view.components.LogoHeader;
import com.skillswap.view.components.StyledButton;
import com.skillswap.view.components.StyledPasswordField;
import com.skillswap.view.components.StyledTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern dark-themed Login View.
 *
 * <p>Follows MVC principles — presents the UI and delegates user interactions
 * to the {@link com.skillswap.controller.AuthController}.
 */
public class LoginPanel extends JPanel {

    private final StyledTextField emailField;
    private final StyledPasswordField passwordField;
    private final StyledButton loginButton;
    private final StyledButton createAccountButton;
    private final JLabel messageLabel;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BG_PRIMARY);

        // Center Card Panel
        CardContainer card = new CardContainer(new EmptyBorder(32, 36, 32, 36));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 520));
        card.setMaximumSize(new Dimension(440, 540));

        // 1. Branding Header
        LogoHeader header = new LogoHeader("Student Skill Exchange Platform");
        card.add(header);

        // 2. Feedback Message Label (Red for errors, Green for success)
        messageLabel = new JLabel(" ");
        messageLabel.setFont(UIConstants.FONT_SMALL);
        messageLabel.setForeground(UIConstants.ACCENT_RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(10));

        // 3. Email Field
        JLabel emailLabel = createFieldLabel("Email Address");
        emailField = new StyledTextField("student@college.edu");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(emailLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));

        // 4. Password Field with Show/Hide Toggle
        JLabel passwordLabel = createFieldLabel("Password");
        passwordField = new StyledPasswordField("Enter your password");
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(24));

        // 5. Login Button
        loginButton = new StyledButton("Sign In", StyledButton.ButtonStyle.PRIMARY);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.INPUT_HEIGHT));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));

        // 6. Navigation Footer ("Don't have an account? Create one")
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel promptLabel = new JLabel("Don't have an account?");
        promptLabel.setFont(UIConstants.FONT_BODY);
        promptLabel.setForeground(UIConstants.TEXT_SECONDARY);

        createAccountButton = new StyledButton("Create Account", StyledButton.ButtonStyle.LINK);

        footerPanel.add(promptLabel);
        footerPanel.add(createAccountButton);
        card.add(footerPanel);

        // Add Enter key listener to submit form
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled()) {
                    loginButton.doClick();
                }
            }
        };
        emailField.addKeyListener(enterSubmit);
        passwordField.getPasswordField().addKeyListener(enterSubmit);

        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_BODY);
        label.setForeground(UIConstants.TEXT_PRIMARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        return label;
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getPasswordString();
    }

    public void setErrorMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_RED);
        messageLabel.setText(message != null ? message : " ");
    }

    public void setSuccessMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_GREEN);
        messageLabel.setText(message != null ? message : " ");
    }

    public void clearMessages() {
        messageLabel.setText(" ");
    }

    public void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        clearMessages();
    }

    public void setLoading(boolean loading) {
        loginButton.setLoading(loading);
        emailField.setEnabled(!loading);
        passwordField.getPasswordField().setEnabled(!loading);
        createAccountButton.setEnabled(!loading);
    }

    public void setLoginAction(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void setNavigateToRegisterAction(ActionListener listener) {
        createAccountButton.addActionListener(listener);
    }
}
