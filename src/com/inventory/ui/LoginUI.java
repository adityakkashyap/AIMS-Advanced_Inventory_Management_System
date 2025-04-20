package com.inventory.ui;

import com.inventory.InventoryFacade;
import com.inventory.db.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Inventory Management System - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Login button
        gbc.gridx = 1; gbc.gridy = 2;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);

        frame.add(panel);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT id, username, role, password FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, use password hashing!

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("id");

                frame.dispose(); // Close login window
                
                // Create and show main UI with appropriate permissions
                InventoryFacade facade = new InventoryFacade();
                InventoryManagementUI mainUI = new InventoryManagementUI(facade, role);
                mainUI.display();
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "Invalid credentials", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, 
                "Database error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void display() {
        frame.setVisible(true);
    }
}