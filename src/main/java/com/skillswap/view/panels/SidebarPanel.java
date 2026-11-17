package com.skillswap.view.panels;

import com.skillswap.model.User;
import com.skillswap.util.UIConstants;
import com.skillswap.view.components.SidebarButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern dark-themed left sidebar containing the SkillSwap logo, student
 * profile snapshot, and the 6 required navigation items.
 */
public class SidebarPanel extends JPanel {

    public static final String TAB_DASHBOARD = "Dashboard";
    public static final String TAB_PROFILE   = "My Profile";
    public static final String TAB_SKILLS    = "My Skills";
    public static final String TAB_FIND      = "Find Students";
    public static final String TAB_REQUESTS  = "Learning Requests";

    private final JLabel studentNameLabel;
    private final JLabel studentEmailLabel;
    private final JLabel studentCollegeLabel;
    private final JLabel avatarLabel;

    private final SidebarButton btnDashboard;
    private final SidebarButton btnProfile;
    private final SidebarButton btnSkills;
    private final SidebarButton btnFind;
    private final SidebarButton btnRequests;
    private final SidebarButton btnLogout;

    private final List<SidebarButton> navButtons = new ArrayList<>();
    private Consumer<String> onTabSelected;
    private Runnable onLogout;

    public SidebarPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_SECONDARY);
        setPreferredSize(new Dimension(250, 750));
        setMinimumSize(new Dimension(220, 600));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER));

        // 1. Top Section: Logo Branding
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(24, 20, 16, 20));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Logo icon badge
        JLabel logoBadge = new JLabel("⇄", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.ACCENT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoBadge.setPreferredSize(new Dimension(34, 34));
        logoBadge.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
        logoBadge.setForeground(new Color(20, 20, 20));

        JLabel appTitle = new JLabel("SkillSwap");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appTitle.setForeground(UIConstants.TEXT_PRIMARY);

        brandRow.add(logoBadge);
        brandRow.add(appTitle);

        JLabel appSubtitle = new JLabel("Student Skill Exchange");
        appSubtitle.setFont(UIConstants.FONT_SMALL);
        appSubtitle.setForeground(UIConstants.TEXT_HINT);
        appSubtitle.setBorder(new EmptyBorder(4, 44, 0, 0));
        appSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(brandRow);
        topPanel.add(appSubtitle);
        topPanel.add(Box.createVerticalStrut(20));

        // 2. Student Info Card Snippet
        JPanel userSnippet = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(38, 38, 38));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(UIConstants.BORDER);
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        userSnippet.setLayout(new BorderLayout(10, 0));
        userSnippet.setOpaque(false);
        userSnippet.setBorder(new EmptyBorder(12, 12, 12, 12));
        userSnippet.setAlignmentX(Component.LEFT_ALIGNMENT);
        userSnippet.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        avatarLabel = new JLabel("ST", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 180, 255, 40));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(UIConstants.ACCENT);
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setPreferredSize(new Dimension(38, 38));
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatarLabel.setForeground(UIConstants.ACCENT);

        JPanel userTextCol = new JPanel();
        userTextCol.setLayout(new BoxLayout(userTextCol, BoxLayout.Y_AXIS));
        userTextCol.setOpaque(false);

        studentNameLabel = new JLabel("Loading...");
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        studentNameLabel.setForeground(UIConstants.TEXT_PRIMARY);

        studentEmailLabel = new JLabel("student@college.edu");
        studentEmailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        studentEmailLabel.setForeground(UIConstants.TEXT_HINT);

        studentCollegeLabel = new JLabel("College");
        studentCollegeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        studentCollegeLabel.setForeground(UIConstants.ACCENT_GREEN);

        userTextCol.add(studentNameLabel);
        userTextCol.add(studentEmailLabel);
        userTextCol.add(studentCollegeLabel);

        userSnippet.add(avatarLabel, BorderLayout.WEST);
        userSnippet.add(userTextCol, BorderLayout.CENTER);

        topPanel.add(userSnippet);
        topPanel.add(Box.createVerticalStrut(16));

        add(topPanel, BorderLayout.NORTH);

        // 3. Center Section: Navigation Menu Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(0, 8, 0, 8));

        btnDashboard = new SidebarButton(TAB_DASHBOARD, "📊");
        btnProfile   = new SidebarButton(TAB_PROFILE, "👤");
        btnSkills    = new SidebarButton(TAB_SKILLS, "💡");
        btnFind      = new SidebarButton(TAB_FIND, "🔍");
        btnRequests  = new SidebarButton(TAB_REQUESTS, "📬");

        navButtons.add(btnDashboard);
        navButtons.add(btnProfile);
        navButtons.add(btnSkills);
        navButtons.add(btnFind);
        navButtons.add(btnRequests);

        for (SidebarButton btn : navButtons) {
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(4));
            btn.addActionListener(e -> selectTab(btn.getText()));
        }

        add(menuPanel, BorderLayout.CENTER);

        // 4. Bottom Section: Logout Button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(16, 8, 20, 8));

        btnLogout = new SidebarButton("Logout", "🚪", true);
        btnLogout.addActionListener(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });

        bottomPanel.add(btnLogout, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Select Dashboard by default
        btnDashboard.setActive(true);
    }

    public void setStudent(User student) {
        if (student != null) {
            String name = student.getName() != null && !student.getName().isBlank() ? student.getName() : "Student";
            studentNameLabel.setText(name);
            studentEmailLabel.setText(student.getEmail() != null ? student.getEmail() : "");
            studentCollegeLabel.setText(student.getCollege() != null ? student.getCollege() : "Student Member");

            // Extract initials
            avatarLabel.setText(getInitials(name));
        } else {
            studentNameLabel.setText("Student");
            studentEmailLabel.setText("");
            studentCollegeLabel.setText("");
            avatarLabel.setText("ST");
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) return "ST";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (parts[0].length() >= 2) {
            return parts[0].substring(0, 2).toUpperCase();
        }
        return parts[0].toUpperCase();
    }

    public void selectTab(String tabName) {
        for (SidebarButton btn : navButtons) {
            btn.setActive(btn.getText().equals(tabName));
        }
        if (onTabSelected != null) {
            onTabSelected.accept(tabName);
        }
    }

    public void setOnTabSelected(Consumer<String> onTabSelected) {
        this.onTabSelected = onTabSelected;
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }
}
