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

    public boolean canViewReports() {
        return this == ADMIN;
    }
}