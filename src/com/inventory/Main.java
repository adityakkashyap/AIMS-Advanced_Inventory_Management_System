package com.inventory;

import com.inventory.ui.LoginUI;

public class Main {
    public static void main(String[] args) {
        // Start with the login UI
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginUI().display();
        });
    }
}