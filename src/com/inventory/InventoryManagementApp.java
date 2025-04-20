package com.inventory;

import com.inventory.ui.LoginUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class InventoryManagementApp {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize and show login window
        SwingUtilities.invokeLater(() -> {
            System.out.println("Initializing Inventory Management System...");
            LoginUI loginUI = new LoginUI();
            loginUI.display();
        });
    }
}