package com.skillswap;

import com.skillswap.model.DashboardStats;
import com.skillswap.model.SkillRequest;
import com.skillswap.model.User;
import com.skillswap.view.components.SidebarButton;
import com.skillswap.view.components.SummaryCard;
import com.skillswap.view.panels.DashboardHomePanel;
import com.skillswap.view.panels.DashboardPanel;
import com.skillswap.view.panels.SidebarPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite verifying the Student Dashboard GUI components,
 * summary cards, sidebar navigation, and state transitions.
 */
public class DashboardGuiTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    @DisplayName("Dashboard Summary Cards: Creation and Value Updates")
    public void testSummaryCards() {
        SummaryCard card = new SummaryCard("Skills Offered", "Ready to teach", "💡", Color.CYAN);
        assertNotNull(card);

        // Setting loading state
        assertDoesNotThrow(card::setLoading);

        // Setting value
        assertDoesNotThrow(() -> card.setValue(5));
    }

    @Test
    @DisplayName("Sidebar Navigation: Buttons and Tab Switching")
    public void testSidebarPanel() {
        SidebarPanel sidebar = new SidebarPanel();
        assertNotNull(sidebar);

        User testUser = new User("uid-123", "Alice Smith", "alice@college.edu", "Harvard");
        assertDoesNotThrow(() -> sidebar.setStudent(testUser));

        // Test tab selection callbacks
        List<String> selectedTabs = new ArrayList<>();
        sidebar.setOnTabSelected(selectedTabs::add);

        sidebar.selectTab(SidebarPanel.TAB_PROFILE);
        assertEquals(1, selectedTabs.size());
        assertEquals(SidebarPanel.TAB_PROFILE, selectedTabs.get(0));

        sidebar.selectTab(SidebarPanel.TAB_DASHBOARD);
        assertEquals(2, selectedTabs.size());
        assertEquals(SidebarPanel.TAB_DASHBOARD, selectedTabs.get(1));
    }

    @Test
    @DisplayName("Dashboard Home Panel: States (Loading, Ready, Error, Empty)")
    public void testDashboardHomePanelStates() {
        DashboardHomePanel homePanel = new DashboardHomePanel();
        assertNotNull(homePanel);

        // State transitions
        assertDoesNotThrow(() -> homePanel.setState(DashboardHomePanel.ViewState.LOADING));
        assertDoesNotThrow(() -> homePanel.setState(DashboardHomePanel.ViewState.READY));
        assertDoesNotThrow(() -> homePanel.setError("Network timeout"));

        // Set student info (with empty skills -> empty states triggered)
        User emptyUser = new User("uid-123", "Bob", "bob@college.edu", "MIT");
        assertDoesNotThrow(() -> homePanel.setStudentInfo(emptyUser));

        // Set stats
        DashboardStats stats = new DashboardStats(3, 4, 2, 1);
        assertDoesNotThrow(() -> homePanel.setStats(stats));

        // Set recent requests
        List<SkillRequest> reqs = new ArrayList<>();
        reqs.add(new SkillRequest("req-1", "uid-123", "Bob", "uid-456", "Charlie", "Java"));
        assertDoesNotThrow(() -> homePanel.setRecentRequests(reqs, "uid-123"));
    }

    @Test
    @DisplayName("Dashboard Panel: Sub-views Integration")
    public void testDashboardPanelSubviews() {
        DashboardPanel dashboard = new DashboardPanel();
        assertNotNull(dashboard.getSidebarPanel());
        assertNotNull(dashboard.getHomePanel());
        assertNotNull(dashboard.getProfilePanel());

        // Sub-tabs switching
        assertDoesNotThrow(() -> dashboard.showSubTab(SidebarPanel.TAB_PROFILE));
        assertDoesNotThrow(() -> dashboard.showSubTab(SidebarPanel.TAB_SKILLS));
        assertDoesNotThrow(() -> dashboard.showSubTab(SidebarPanel.TAB_FIND));
        assertDoesNotThrow(() -> dashboard.showSubTab(SidebarPanel.TAB_REQUESTS));
        assertDoesNotThrow(() -> dashboard.showSubTab(SidebarPanel.TAB_DASHBOARD));
    }
}
