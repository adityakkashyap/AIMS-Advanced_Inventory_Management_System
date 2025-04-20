package com.inventory.repository;

import com.inventory.db.DatabaseConnection;
import com.inventory.model.OrderData;
import java.sql.*;
import java.util.Map;

public class OrderRepository {
    
    public boolean createOrder(OrderData orderData) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Create order
            String orderQuery = "INSERT INTO orders (status) VALUES ('PENDING')";
            PreparedStatement orderStmt = conn.prepareStatement(orderQuery, Statement.RETURN_GENERATED_KEYS);
            orderStmt.executeUpdate();
            
            // Get the generated order ID
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

            // Add order items
            for (Map.Entry<Integer, Integer> item : orderData.getItems().entrySet()) {
                int productId = item.getKey();
                int quantity = item.getValue();
                
                // Get product price
                double price = getProductPrice(productId);
                
                // Insert order item
                String itemQuery = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.setDouble(4, price);
                itemStmt.executeUpdate();
                
                // Update product stock
                String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStockQuery);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, productId);
                updateStmt.executeUpdate();
            }
            
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private double getProductPrice(int productId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT price FROM products WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
            return 0.0;
        }
    }
}