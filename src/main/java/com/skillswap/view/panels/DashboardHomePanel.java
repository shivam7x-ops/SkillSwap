package com.skillswap.view.panels;

import com.skillswap.model.DashboardStats;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import com.skillswap.util.UIConstants;
import com.skillswap.view.components.CardContainer;
import com.skillswap.view.components.SkillChip;
import com.skillswap.view.components.StyledButton;
import com.skillswap.view.components.SummaryCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Main overview panel inside the Student Dashboard.
 *
 * <p>Contains:
 * <ul>
 *   <li>Welcome banner with student's actual name, email, and college</li>
 *   <li>4 Metric summary cards (Offered, Wanted, Connections, Requests)</li>
 *   <li>Skills quick-look with pill tags</li>
 *   <li>Recent exchange activity</li>
 *   <li>Explicit Loading, Empty, and Error states</li>
 * </ul>
 */
public class DashboardHomePanel extends JPanel {

    public enum ViewState {
        LOADING, ERROR, READY
    }

    private final JLabel welcomeTitleLabel;
    private final JLabel welcomeSubtitleLabel;
    private final StyledButton refreshButton;

    // Summary Cards
    private final SummaryCard cardSkillsOffered;
    private final SummaryCard cardSkillsWanted;
    private final SummaryCard cardActiveConnections;
    private final SummaryCard cardPendingRequests;

    // State panels
    private final JPanel stateContainer;
    private final CardLayout stateCardLayout;
    private final JPanel loadingPanel;
    private final JPanel errorPanel;
    private final JLabel errorLabel;
    private final StyledButton retryButton;
    private final JPanel contentPanel;

    // Skills preview containers
    private final JPanel teachSkillsPanel;
    private final JPanel learnSkillsPanel;
    private final JPanel recentRequestsPanel;

    private Runnable onRefreshRequested;

    public DashboardHomePanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. Top Welcome Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel titleCol = new JPanel();
        titleCol.setLayout(new BoxLayout(titleCol, BoxLayout.Y_AXIS));
        titleCol.setOpaque(false);

        welcomeTitleLabel = new JLabel("Welcome back!");
        welcomeTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        welcomeTitleLabel.setForeground(UIConstants.TEXT_PRIMARY);

        welcomeSubtitleLabel = new JLabel("Student Skill Exchange Dashboard");
        welcomeSubtitleLabel.setFont(UIConstants.FONT_BODY);
        welcomeSubtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);

        titleCol.add(welcomeTitleLabel);
        titleCol.add(Box.createVerticalStrut(4));
        titleCol.add(welcomeSubtitleLabel);

        refreshButton = new StyledButton("↻ Refresh", StyledButton.ButtonStyle.SECONDARY);
        refreshButton.setPreferredSize(new Dimension(110, 36));
        refreshButton.addActionListener(e -> {
            if (onRefreshRequested != null) {
                onRefreshRequested.run();
            }
        });

        headerPanel.add(titleCol, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. States Switcher Container (CardLayout: LOADING, ERROR, READY)
        stateCardLayout = new CardLayout();
        stateContainer = new JPanel(stateCardLayout);
        stateContainer.setOpaque(false);

        // A. Loading State Panel
        loadingPanel = createLoadingPanel();
        stateContainer.add(loadingPanel, ViewState.LOADING.name());

        // B. Error State Panel
        errorLabel = new JLabel("Failed to load dashboard data");
        errorLabel.setFont(UIConstants.FONT_BODY);
        errorLabel.setForeground(UIConstants.ACCENT_RED);

        retryButton = new StyledButton("Try Again", StyledButton.ButtonStyle.PRIMARY);
        retryButton.setPreferredSize(new Dimension(140, 38));
        retryButton.addActionListener(e -> {
            if (onRefreshRequested != null) {
                onRefreshRequested.run();
            }
        });

        errorPanel = createErrorPanel();
        stateContainer.add(errorPanel, ViewState.ERROR.name());

        // C. Ready Content Panel (Scrollable)
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Grid of 4 Summary Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        cardSkillsOffered = new SummaryCard("Skills Offered", "Skills you can teach", "💡", UIConstants.ACCENT);
        cardSkillsWanted = new SummaryCard("Skills Wanted", "Skills to learn", "🎯", UIConstants.ACCENT_GREEN);
        cardActiveConnections = new SummaryCard("Active Connections", "Accepted matches", "🤝", UIConstants.ACCENT_YELLOW);
        cardPendingRequests = new SummaryCard("Pending Requests", "Awaiting response", "📬", new Color(255, 140, 60));

        cardsGrid.add(cardSkillsOffered);
        cardsGrid.add(cardSkillsWanted);
        cardsGrid.add(cardActiveConnections);
        cardsGrid.add(cardPendingRequests);

        contentPanel.add(cardsGrid);
        contentPanel.add(Box.createVerticalStrut(24));

        // Two Column Section: Quick Skills Look
        JPanel skillsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        skillsRow.setOpaque(false);
        skillsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Teach Skills Card
        CardContainer teachCard = new CardContainer(new EmptyBorder(16, 18, 16, 18));
        teachCard.setLayout(new BorderLayout());
        JLabel teachTitle = new JLabel("💡 Skills I Can Teach");
        teachTitle.setFont(UIConstants.FONT_SUBTITLE);
        teachTitle.setForeground(UIConstants.ACCENT);
        teachCard.add(teachTitle, BorderLayout.NORTH);

        teachSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        teachSkillsPanel.setOpaque(false);
        teachSkillsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        teachCard.add(teachSkillsPanel, BorderLayout.CENTER);

        // Learn Skills Card
        CardContainer learnCard = new CardContainer(new EmptyBorder(16, 18, 16, 18));
        learnCard.setLayout(new BorderLayout());
        JLabel learnTitle = new JLabel("🎯 Skills I Want to Learn");
        learnTitle.setFont(UIConstants.FONT_SUBTITLE);
        learnTitle.setForeground(UIConstants.ACCENT_GREEN);
        learnCard.add(learnTitle, BorderLayout.NORTH);

        learnSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        learnSkillsPanel.setOpaque(false);
        learnSkillsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        learnCard.add(learnSkillsPanel, BorderLayout.CENTER);

        skillsRow.add(teachCard);
        skillsRow.add(learnCard);

        contentPanel.add(skillsRow);
        contentPanel.add(Box.createVerticalStrut(24));

        // Recent Requests Card
        CardContainer requestsCard = new CardContainer(new EmptyBorder(16, 20, 16, 20));
        requestsCard.setLayout(new BorderLayout());
        JLabel requestsTitle = new JLabel("📬 Recent Learning Requests Activity");
        requestsTitle.setFont(UIConstants.FONT_SUBTITLE);
        requestsTitle.setForeground(UIConstants.TEXT_PRIMARY);
        requestsCard.add(requestsTitle, BorderLayout.NORTH);

        recentRequestsPanel = new JPanel();
        recentRequestsPanel.setLayout(new BoxLayout(recentRequestsPanel, BoxLayout.Y_AXIS));
        recentRequestsPanel.setOpaque(false);
        recentRequestsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        requestsCard.add(recentRequestsPanel, BorderLayout.CENTER);

        contentPanel.add(requestsCard);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        stateContainer.add(scrollPane, ViewState.READY.name());

        add(stateContainer, BorderLayout.CENTER);

        // Initial state
        setState(ViewState.LOADING);
    }

    private JPanel createLoadingPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel spinnerLabel = new JLabel("⏳", SwingConstants.CENTER);
        spinnerLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        spinnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel text = new JLabel("Loading student dashboard from Firebase...");
        text.setFont(UIConstants.FONT_BODY);
        text.setForeground(UIConstants.TEXT_SECONDARY);
        text.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(spinnerLabel);
        box.add(Box.createVerticalStrut(12));
        box.add(text);
        p.add(box);
        return p;
    }

    private JPanel createErrorPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);

        JLabel icon = new JLabel("⚠️", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(icon);
        box.add(Box.createVerticalStrut(10));
        box.add(errorLabel);
        box.add(Box.createVerticalStrut(16));
        box.add(retryButton);
        p.add(box);
        return p;
    }

    public void setState(ViewState state) {
        stateCardLayout.show(stateContainer, state.name());
        refreshButton.setEnabled(state != ViewState.LOADING);
    }

    public void setError(String errorMessage) {
        errorLabel.setText(errorMessage != null ? errorMessage : "Could not load data from Firebase.");
        setState(ViewState.ERROR);
    }

    /**
     * Updates the welcome section with the student's actual credentials.
     */
    public void setStudentInfo(User user) {
        if (user != null) {
            String name = user.getName() != null && !user.getName().isBlank() ? user.getName() : "Student";
            welcomeTitleLabel.setText("Welcome back, " + name + "! 👋");

            String college = user.getCollege() != null && !user.getCollege().isBlank() ? user.getCollege() : "College";
            String email = user.getEmail() != null ? user.getEmail() : "";
            welcomeSubtitleLabel.setText(college + " • " + email);

            // Populate Teach Skills chips
            teachSkillsPanel.removeAll();
            List<String> teach = user.getSkillsToTeach();
            if (teach != null && !teach.isEmpty()) {
                for (String s : teach) {
                    teachSkillsPanel.add(new SkillChip(s, SkillChip.ChipType.TEACH));
                }
            } else {
                // Empty state
                JLabel emptyTeach = new JLabel("No skills added yet. Head to 'My Skills' to list what you can teach.");
                emptyTeach.setFont(UIConstants.FONT_SMALL);
                emptyTeach.setForeground(UIConstants.TEXT_HINT);
                teachSkillsPanel.add(emptyTeach);
            }

            // Populate Learn Skills chips
            learnSkillsPanel.removeAll();
            List<String> learn = user.getSkillsToLearn();
            if (learn != null && !learn.isEmpty()) {
                for (String s : learn) {
                    learnSkillsPanel.add(new SkillChip(s, SkillChip.ChipType.LEARN));
                }
            } else {
                // Empty state
                JLabel emptyLearn = new JLabel("No skills added yet. Head to 'My Skills' to list what you want to learn.");
                emptyLearn.setFont(UIConstants.FONT_SMALL);
                emptyLearn.setForeground(UIConstants.TEXT_HINT);
                learnSkillsPanel.add(emptyLearn);
            }

            teachSkillsPanel.revalidate();
            teachSkillsPanel.repaint();
            learnSkillsPanel.revalidate();
            learnSkillsPanel.repaint();
        }
    }

    /**
     * Updates the 4 metric summary cards.
     */
    public void setStats(DashboardStats stats) {
        if (stats != null) {
            cardSkillsOffered.setValue(stats.getSkillsOfferedCount());
            cardSkillsWanted.setValue(stats.getSkillsWantedCount());
            cardActiveConnections.setValue(stats.getActiveConnectionsCount());
            cardPendingRequests.setValue(stats.getPendingRequestsCount());
        }
    }

    /**
     * Updates the recent activity list.
     */
    public void setRecentRequests(List<SkillRequest> requests, String currentUid) {
        recentRequestsPanel.removeAll();

        if (requests == null || requests.isEmpty()) {
            // Empty state
            JPanel emptyBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
            emptyBox.setOpaque(false);
            JLabel emptyLabel = new JLabel("No recent learning requests. Use 'Find Students' to discover learning partners!");
            emptyLabel.setFont(UIConstants.FONT_SMALL);
            emptyLabel.setForeground(UIConstants.TEXT_HINT);
            emptyBox.add(emptyLabel);
            recentRequestsPanel.add(emptyBox);
        } else {
            SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy");
            int count = Math.min(requests.size(), 5);

            for (int i = 0; i < count; i++) {
                SkillRequest req = requests.get(i);
                boolean isOutgoing = currentUid != null && currentUid.equals(req.getSenderUid());

                JPanel item = new JPanel(new BorderLayout(10, 0));
                item.setOpaque(false);
                item.setBorder(new EmptyBorder(8, 0, 8, 0));

                String partnerName = isOutgoing ? req.getReceiverName() : req.getSenderName();
                String directionText = isOutgoing ? "Sent to " + partnerName : "From " + partnerName;

                JLabel title = new JLabel((isOutgoing ? "↗ " : "↙ ") + req.getSkill() + " (" + directionText + ")");
                title.setFont(new Font("Segoe UI", Font.BOLD, 13));
                title.setForeground(UIConstants.TEXT_PRIMARY);

                String dateStr = req.getTimestamp() > 0 ? df.format(new Date(req.getTimestamp())) : "Recent";
                JLabel statusBadge = new JLabel(req.getStatus() + " • " + dateStr);
                statusBadge.setFont(UIConstants.FONT_SMALL);

                if (SkillRequest.Status.ACCEPTED.name().equalsIgnoreCase(req.getStatus())) {
                    statusBadge.setForeground(UIConstants.ACCENT_GREEN);
                } else if (SkillRequest.Status.REJECTED.name().equalsIgnoreCase(req.getStatus())) {
                    statusBadge.setForeground(UIConstants.ACCENT_RED);
                } else {
                    statusBadge.setForeground(UIConstants.ACCENT_YELLOW);
                }

                item.add(title, BorderLayout.WEST);
                item.add(statusBadge, BorderLayout.EAST);

                recentRequestsPanel.add(item);
                if (i < count - 1) {
                    JSeparator sep = new JSeparator();
                    sep.setForeground(new Color(60, 60, 60));
                    recentRequestsPanel.add(sep);
                }
            }
        }

        recentRequestsPanel.revalidate();
        recentRequestsPanel.repaint();
    }

    public void setOnRefreshRequested(Runnable onRefreshRequested) {
        this.onRefreshRequested = onRefreshRequested;
    }
}
