package com.skillswap.view.panels;

import com.skillswap.model.User;
import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel displaying the student's personal profile information retrieved from Firebase.
 */
public class ProfileViewPanel extends JPanel {

    private final JLabel nameValueLabel;
    private final JLabel emailValueLabel;
    private final JLabel collegeValueLabel;
    private final JLabel uidValueLabel;

    public ProfileViewPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel title = new JLabel("👤 Student Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Your verified student profile information");
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

        // Profile Details Card
        CardContainer card = new CardContainer(new EmptyBorder(24, 28, 24, 28));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(650, 400));

        nameValueLabel = createValueLabel();
        emailValueLabel = createValueLabel();
        collegeValueLabel = createValueLabel();
        uidValueLabel = createValueLabel();

        addProfileRow(card, "Full Name", nameValueLabel);
        addProfileRow(card, "Email Address", emailValueLabel);
        addProfileRow(card, "College / University", collegeValueLabel);
        addProfileRow(card, "Firebase Student UID", uidValueLabel);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerWrap.setOpaque(false);
        centerWrap.add(card);

        add(centerWrap, BorderLayout.CENTER);
    }

    private JLabel createValueLabel() {
        JLabel l = new JLabel("-");
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UIConstants.TEXT_PRIMARY);
        return l;
    }

    private void addProfileRow(JPanel container, String labelText, JLabel valueLabel) {
        JLabel rowLabel = new JLabel(labelText);
        rowLabel.setFont(UIConstants.FONT_SMALL);
        rowLabel.setForeground(UIConstants.TEXT_SECONDARY);

        container.add(rowLabel);
        container.add(Box.createVerticalStrut(4));
        container.add(valueLabel);
        container.add(Box.createVerticalStrut(18));
    }

    public void setStudent(User student) {
        if (student != null) {
            nameValueLabel.setText(student.getName() != null ? student.getName() : "Not specified");
            emailValueLabel.setText(student.getEmail() != null ? student.getEmail() : "Not specified");
            collegeValueLabel.setText(student.getCollege() != null ? student.getCollege() : "Not specified");
            uidValueLabel.setText(student.getUid() != null ? student.getUid() : "Not specified");
        }
    }
}
