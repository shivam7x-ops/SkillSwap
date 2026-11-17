package com.skillswap.view.panels;

import com.skillswap.util.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level Student Dashboard view composed of the left {@link SidebarPanel}
 * and a center container that toggles between sub-views.
 */
public class DashboardPanel extends JPanel {

    private final SidebarPanel sidebarPanel;
    private final CardLayout contentCardLayout;
    private final JPanel centerContainer;

    private final DashboardHomePanel homePanel;
    private final ProfilePanel profilePanel;
    private final SkillsManagementPanel skillsPanel;
    private final PlaceholderSubPanel findPanel;
    private final PlaceholderSubPanel requestsPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_PRIMARY);

        // 1. Left Sidebar
        sidebarPanel = new SidebarPanel();
        add(sidebarPanel, BorderLayout.WEST);

        // 2. Center Content Cards
        contentCardLayout = new CardLayout();
        centerContainer = new JPanel(contentCardLayout);
        centerContainer.setOpaque(false);

        homePanel = new DashboardHomePanel();
        profilePanel = new ProfilePanel();
        skillsPanel = new SkillsManagementPanel();
        findPanel = new PlaceholderSubPanel("Find Students", "🔍", "Explore students with complementary skills for swapping.");
        requestsPanel = new PlaceholderSubPanel("Learning Requests", "📬", "Review incoming and outgoing exchange requests.");

        centerContainer.add(homePanel, SidebarPanel.TAB_DASHBOARD);
        centerContainer.add(profilePanel, SidebarPanel.TAB_PROFILE);
        centerContainer.add(skillsPanel, SidebarPanel.TAB_SKILLS);
        centerContainer.add(findPanel, SidebarPanel.TAB_FIND);
        centerContainer.add(requestsPanel, SidebarPanel.TAB_REQUESTS);

        add(centerContainer, BorderLayout.CENTER);

        // Sidebar tab selection hook
        sidebarPanel.setOnTabSelected(this::showSubTab);
    }

    public void showSubTab(String tabName) {
        contentCardLayout.show(centerContainer, tabName);
    }

    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public DashboardHomePanel getHomePanel() {
        return homePanel;
    }

    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    public SkillsManagementPanel getSkillsPanel() {
        return skillsPanel;
    }
}
