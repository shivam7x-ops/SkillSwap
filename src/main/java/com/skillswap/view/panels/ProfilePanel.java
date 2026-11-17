package com.skillswap.view.panels;

import com.skillswap.model.User;
import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;
import com.skillswap.view.components.StyledButton;
import com.skillswap.view.components.StyledTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Modern dark-themed panel for viewing and editing student profile information.
 *
 * <p>Requirements:
 * <ul>
 *   <li>Display actual logged-in student info (Name, Email, College, Firebase UID).</li>
 *   <li>Allow editing Full Name and College Name.</li>
 *   <li>Email is strictly read-only.</li>
 *   <li>Save Profile and Cancel buttons with loading & notification states.</li>
 *   <li>Store and retrieve from Firebase Realtime Database scoped by UID.</li>
 * </ul>
 */
public class ProfilePanel extends JPanel {

    private final StyledTextField nameField;
    private final StyledTextField emailField;
    private final StyledTextField collegeField;
    private final JLabel uidValueLabel;

    private final StyledButton btnSave;
    private final StyledButton btnCancel;
    private final JLabel messageLabel;

    private User currentStudent;
    private Consumer<ProfilePanel> onSaveListener;
    private Runnable onCancelListener;

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // 1. Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("👤 My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("View and manage your student account credentials and college affiliation");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);
        headerPanel.add(titleBox, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content (Card)
        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerWrap.setOpaque(false);

        CardContainer card = new CardContainer(new EmptyBorder(28, 32, 28, 32));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(600, 520));
        card.setMaximumSize(new Dimension(650, 560));

        // Feedback / Notification Banner
        messageLabel = new JLabel(" ");
        messageLabel.setFont(UIConstants.FONT_SMALL);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(messageLabel);
        card.add(Box.createVerticalStrut(14));

        // Full Name Field (Editable)
        card.add(createFieldLabel("Full Name", "✏️ Editable"));
        card.add(Box.createVerticalStrut(6));
        nameField = new StyledTextField("Enter your full name");
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameField);
        card.add(Box.createVerticalStrut(18));

        // Email Field (Read-Only)
        card.add(createFieldLabel("Email Address", "🔒 Read-only (Firebase Auth)"));
        card.add(Box.createVerticalStrut(6));
        emailField = new StyledTextField("Email address");
        emailField.setEditable(false);
        emailField.setFocusable(false);
        emailField.setForeground(UIConstants.TEXT_SECONDARY);
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(emailField);
        card.add(Box.createVerticalStrut(18));

        // College Field (Editable)
        card.add(createFieldLabel("College / University", "✏️ Editable"));
        card.add(Box.createVerticalStrut(6));
        collegeField = new StyledTextField("Enter your college or university name");
        collegeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(collegeField);
        card.add(Box.createVerticalStrut(18));

        // Firebase UID (Read-Only Info)
        card.add(createFieldLabel("Firebase Student UID", "🔑 Unique Identifier"));
        card.add(Box.createVerticalStrut(6));
        uidValueLabel = new JLabel("Not loaded");
        uidValueLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        uidValueLabel.setForeground(UIConstants.TEXT_HINT);
        uidValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(uidValueLabel);
        card.add(Box.createVerticalStrut(28));

        // Action Buttons Row
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonsRow.setOpaque(false);
        buttonsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnSave = new StyledButton("💾 Save Profile", StyledButton.ButtonStyle.PRIMARY);
        btnSave.setPreferredSize(new Dimension(160, UIConstants.INPUT_HEIGHT));
        btnSave.addActionListener(e -> {
            if (onSaveListener != null) {
                onSaveListener.accept(this);
            }
        });

        btnCancel = new StyledButton("✕ Cancel", StyledButton.ButtonStyle.SECONDARY);
        btnCancel.setPreferredSize(new Dimension(120, UIConstants.INPUT_HEIGHT));
        btnCancel.addActionListener(e -> {
            resetFields();
            clearMessage();
            if (onCancelListener != null) {
                onCancelListener.run();
            }
        });

        buttonsRow.add(btnSave);
        buttonsRow.add(btnCancel);
        card.add(buttonsRow);

        centerWrap.add(card);

        // Put card into a scroll pane for smaller resolutions
        JScrollPane scrollPane = new JScrollPane(centerWrap);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createFieldLabel(String labelText, String badgeText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(UIConstants.TEXT_PRIMARY);
        row.add(label);

        if (badgeText != null) {
            JLabel badge = new JLabel("(" + badgeText + ")");
            badge.setFont(UIConstants.FONT_SMALL);
            badge.setForeground(UIConstants.TEXT_HINT);
            row.add(badge);
        }

        return row;
    }

    /**
     * Populates the profile view with current student data.
     */
    public void setStudent(User student) {
        this.currentStudent = student;
        if (student != null) {
            nameField.setText(student.getName() != null ? student.getName() : "");
            emailField.setText(student.getEmail() != null ? student.getEmail() : "");
            collegeField.setText(student.getCollege() != null ? student.getCollege() : "");
            uidValueLabel.setText(student.getUid() != null ? student.getUid() : "N/A");
        }
    }

    /**
     * Resets input fields back to currentStudent values.
     */
    public void resetFields() {
        if (currentStudent != null) {
            nameField.setText(currentStudent.getName() != null ? currentStudent.getName() : "");
            collegeField.setText(currentStudent.getCollege() != null ? currentStudent.getCollege() : "");
        }
    }

    public String getFullName() {
        return nameField.getText() != null ? nameField.getText().trim() : "";
    }

    public String getCollege() {
        return collegeField.getText() != null ? collegeField.getText().trim() : "";
    }

    public String getEmail() {
        return emailField.getText() != null ? emailField.getText().trim() : "";
    }

    public String getUid() {
        return uidValueLabel.getText();
    }

    public void setLoading(boolean loading) {
        btnSave.setLoading(loading);
        btnCancel.setEnabled(!loading);
        nameField.setEnabled(!loading);
        collegeField.setEnabled(!loading);
    }

    public void setSuccessMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_GREEN);
        messageLabel.setText("✓ " + message);
    }

    public void setErrorMessage(String message) {
        messageLabel.setForeground(UIConstants.ACCENT_RED);
        messageLabel.setText("✗ " + message);
    }

    public void clearMessage() {
        messageLabel.setText(" ");
    }

    public void setOnSave(Consumer<ProfilePanel> listener) {
        this.onSaveListener = listener;
    }

    public void setOnCancel(Runnable listener) {
        this.onCancelListener = listener;
    }

    public StyledTextField getNameField() {
        return nameField;
    }

    public StyledTextField getCollegeField() {
        return collegeField;
    }

    public StyledButton getBtnSave() {
        return btnSave;
    }

    public StyledButton getBtnCancel() {
        return btnCancel;
    }
}
