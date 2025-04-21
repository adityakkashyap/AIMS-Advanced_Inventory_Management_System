package com.inventory.model;

public enum UserRole {
    ADMIN("admin"),
    INVENTORY("inventory"),
    SALES("sales");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }

    public boolean canManageProducts() {
        return this == ADMIN || this == INVENTORY;
    }

    public boolean canManageOrders() {
        return this == ADMIN || this == SALES;
    }

    public boolean canViewReport(String reportType) {
        // Admin can view all reports
        if (this == ADMIN) {
            return true;
        }
        // Sales can only view sales reports
        if (this == SALES && reportType.equalsIgnoreCase("sales")) {
            return true;
        }
        // Inventory can only view inventory reports
        if (this == INVENTORY && reportType.equalsIgnoreCase("inventory")) {
            return true;
        }
        return false;
    }

    // Deprecate this method in favor of canViewReport(String)
    @Deprecated
    public boolean canViewReports() {
        return this == ADMIN;
    }
}