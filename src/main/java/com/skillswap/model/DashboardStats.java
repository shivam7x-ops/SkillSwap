package com.skillswap.model;

/**
 * Data transfer model representing key metrics displayed on the Student Dashboard.
 */
public class DashboardStats {

    private final int skillsOfferedCount;
    private final int skillsWantedCount;
    private final int activeConnectionsCount;
    private final int pendingRequestsCount;

    public DashboardStats(int skillsOfferedCount, int skillsWantedCount,
                          int activeConnectionsCount, int pendingRequestsCount) {
        this.skillsOfferedCount = skillsOfferedCount;
        this.skillsWantedCount = skillsWantedCount;
        this.activeConnectionsCount = activeConnectionsCount;
        this.pendingRequestsCount = pendingRequestsCount;
    }

    public static DashboardStats empty() {
        return new DashboardStats(0, 0, 0, 0);
    }

    public int getSkillsOfferedCount() {
        return skillsOfferedCount;
    }

    public int getSkillsWantedCount() {
        return skillsWantedCount;
    }

    public int getActiveConnectionsCount() {
        return activeConnectionsCount;
    }

    public int getPendingRequestsCount() {
        return pendingRequestsCount;
    }

    @Override
    public String toString() {
        return "DashboardStats{" +
                "skillsOffered=" + skillsOfferedCount +
                ", skillsWanted=" + skillsWantedCount +
                ", activeConnections=" + activeConnectionsCount +
                ", pendingRequests=" + pendingRequestsCount +
                '}';
    }
}
