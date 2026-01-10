package com.dmcdoc.sharedcommon.dto;

public class AdminStatsResponse {

    private long totalUsers;
    private long admins;
    private long disabled;

    public AdminStatsResponse() {
    }

    public AdminStatsResponse(long totalUsers, long admins, long disabled) {
        this.totalUsers = totalUsers;
        this.admins = admins;
        this.disabled = disabled;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getAdmins() {
        return admins;
    }

    public void setAdmins(long admins) {
        this.admins = admins;
    }

    public long getDisabled() {
        return disabled;
    }

    public void setDisabled(long disabled) {
        this.disabled = disabled;
    }
}
