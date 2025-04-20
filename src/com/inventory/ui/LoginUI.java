package com.inventory.ui;

import com.inventory.InventoryFacade;
import com.inventory.db.DatabaseConnection;
import com.inventory.model.UserRole;
import com.inventory.repository.UserRepository;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginUI {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private final UserRepository userRepository;

    public LoginUI() {
        this.userRepository = new UserRepository();
        initializeUI();
    }

    private void handleLogin(String username, String password) {
        if (userRepository.validateCredentials(username, password)) {
            String roleStr = userRepository.getUserRole(username);
            try {
                UserRole role = UserRole.fromString(roleStr);
                InventoryManagementUI ui = new InventoryManagementUI(new InventoryFacade(), role);
                ui.display();
                frame.dispose();
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(frame,
                    "Invalid role configuration",
                    "System Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame,
                "Invalid credentials",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
        }
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
        loginButton.addActionListener(e -> 
            handleLogin(usernameField.getText(), new String(passwordField.getPassword()))
        );
        panel.add(loginButton, gbc);

        frame.add(panel);
    }

    public void display() {
        frame.setVisible(true);
    }
}