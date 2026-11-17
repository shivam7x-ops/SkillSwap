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
 * Modern dark-themed Registration View.
 *
 * <p>Allows students to register with Full Name, Email, College, Password, and Confirm Password.
 * Communicates with {@link com.skillswap.controller.AuthController} using MVC principles.
 */
public class RegisterPanel extends JPanel {

    private final StyledTextField nameField;
    private final StyledTextField emailField;
    private final StyledTextField collegeField;
    private final StyledPasswordField passwordField;
    private final StyledPasswordField confirmPasswordField;
    private final StyledButton registerButton;
    private final StyledButton backToLoginButton;
    private final JLabel messageLabel;

    public RegisterPanel() {
        setLayout(new GridBagLayout());
        setBackground(UIConstants.BG_PRIMARY);

        // Center Card Panel
        CardContainer card = new CardContainer(new EmptyBorder(24, 36, 24, 36));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 640));
        card.setMaximumSize(new Dimension(480, 660));

        // 1. Branding Header
        LogoHeader header = new LogoHeader("Join the Student Skill Exchange");
        card.add(header);

        // 2. Feedback Message Label
        messageLabel = new JLabel(" ");
        messageLabel.setFont(UIConstants.FONT_SMALL);
        messageLabel.setForeground(UIConstants.ACCENT_RED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(6));

        // 3. Full Name
        card.add(createFieldLabel("Full Name"));
        card.add(Box.createVerticalStrut(3));
        nameField = new StyledTextField("e.g. Alex Johnson");
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameField);
        card.add(Box.createVerticalStrut(10));

        // 4. Email Address
        card.add(createFieldLabel("College Email Address"));
        card.add(Box.createVerticalStrut(3));
        emailField = new StyledTextField("e.g. alex@college.edu");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(emailField);
        card.add(Box.createVerticalStrut(10));

        // 5. College Name
        card.add(createFieldLabel("College / University"));
        card.add(Box.createVerticalStrut(3));
        collegeField = new StyledTextField("e.g. Stanford University");
        collegeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(collegeField);
        card.add(Box.createVerticalStrut(10));

        // 6. Password
        card.add(createFieldLabel("Password (min 6 characters)"));
        card.add(Box.createVerticalStrut(3));
        passwordField = new StyledPasswordField("Create a strong password");
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        // 7. Confirm Password
        card.add(createFieldLabel("Confirm Password"));
        card.add(Box.createVerticalStrut(3));
        confirmPasswordField = new StyledPasswordField("Repeat your password");
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(confirmPasswordField);
        card.add(Box.createVerticalStrut(18));

        // 8. Register Button
        registerButton = new StyledButton("Create Account", StyledButton.ButtonStyle.PRIMARY);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIConstants.INPUT_HEIGHT));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(14));

        // 9. Navigation Footer ("Already have an account? Sign In")
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);
        footerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel promptLabel = new JLabel("Already have an account?");
        promptLabel.setFont(UIConstants.FONT_BODY);
        promptLabel.setForeground(UIConstants.TEXT_SECONDARY);

        backToLoginButton = new StyledButton("Sign In", StyledButton.ButtonStyle.LINK);

        footerPanel.add(promptLabel);
        footerPanel.add(backToLoginButton);
        card.add(footerPanel);

        // Enter key to submit
        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && registerButton.isEnabled()) {
                    registerButton.doClick();
                }
            }
        };
        nameField.addKeyListener(enterSubmit);
        emailField.addKeyListener(enterSubmit);
        collegeField.addKeyListener(enterSubmit);
        passwordField.getPasswordField().addKeyListener(enterSubmit);
        confirmPasswordField.getPasswordField().addKeyListener(enterSubmit);

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

    public String getFullName() {
        return nameField.getText().trim();
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getCollege() {
        return collegeField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getPasswordString();
    }

    public String getConfirmPassword() {
        return confirmPasswordField.getPasswordString();
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
        nameField.setText("");
        emailField.setText("");
        collegeField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        clearMessages();
    }

    public void setLoading(boolean loading) {
        registerButton.setLoading(loading);
        nameField.setEnabled(!loading);
        emailField.setEnabled(!loading);
        collegeField.setEnabled(!loading);
        passwordField.getPasswordField().setEnabled(!loading);
        confirmPasswordField.getPasswordField().setEnabled(!loading);
        backToLoginButton.setEnabled(!loading);
    }

    public void setRegisterAction(ActionListener listener) {
        registerButton.addActionListener(listener);
    }

    public void setNavigateToLoginAction(ActionListener listener) {
        backToLoginButton.addActionListener(listener);
    }
}
