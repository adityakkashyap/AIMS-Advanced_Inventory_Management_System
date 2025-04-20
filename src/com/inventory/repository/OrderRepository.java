package com.inventory.repository;

import com.inventory.db.DatabaseConnection;
import com.inventory.model.OrderData;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrderRepository {
    
    public Map<String, Map<String, Object>> getDetailedSalesData() {
        Map<String, Map<String, Object>> salesData = new HashMap<>();
        
        String query = 
            "SELECT p.description, " +
            "COALESCE(SUM(oi.quantity), 0) as total_quantity, " +
            "COALESCE(SUM(oi.quantity * p.price), 0) as total_revenue, " +
            "COUNT(DISTINCT oi.order_id) as order_count " +  // Changed from customer_id to order_id
            "FROM products p " +
            "LEFT JOIN order_items oi ON p.id = oi.product_id " +
            "LEFT JOIN orders o ON oi.order_id = o.id " +
            "GROUP BY p.description";
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("quantity", rs.getInt("total_quantity"));
                data.put("revenue", rs.getDouble("total_revenue"));
                data.put("orders", rs.getString("order_count"));
                
                salesData.put(rs.getString("description"), data);
            }
            
            if (salesData.isEmpty()) {
                Map<String, Object> dummyData = new HashMap<>();
                dummyData.put("quantity", 0);
                dummyData.put("revenue", 0.0);
                dummyData.put("orders", "0");
                salesData.put("No sales data available", dummyData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("quantity", 0);
            errorData.put("revenue", 0.0);
            errorData.put("orders", "Error");
            salesData.put("Database error occurred", errorData);
        }
        
        return salesData;
    }

    public Map<String, Double> getMonthlyTrend() {
        Map<String, Double> monthlyTrend = new LinkedHashMap<>();
        
        String query = 
            "SELECT DATE_FORMAT(o.order_date, '%Y-%m') as month, " +
            "COALESCE(SUM(oi.quantity * p.price), 0) as monthly_revenue " +
            "FROM orders o " +
            "JOIN order_items oi ON o.id = oi.order_id " +
            "JOIN products p ON oi.product_id = p.id " +
            "GROUP BY month " +
            "ORDER BY month DESC " +
            "LIMIT 6";
            
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                monthlyTrend.put(
                    rs.getString("month"),
                    rs.getDouble("monthly_revenue")
                );
            }
            
            if (monthlyTrend.isEmpty()) {
                // Just add current month with 0 if no data
                monthlyTrend.put(new java.text.SimpleDateFormat("yyyy-MM")
                    .format(new java.util.Date()), 0.0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            monthlyTrend.put("Error", 0.0);
        }
        
        return monthlyTrend;
    }


    public boolean createOrder(OrderData orderData) {
        String orderSql = "INSERT INTO orders (order_date) VALUES (?)";
        // Remove price from SQL as it's not in the table
        String itemsSql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";
        String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // First, validate stock for all items
            for (Map.Entry<Integer, Integer> item : orderData.getItems().entrySet()) {
                int productId = item.getKey();
                int quantity = item.getValue();
                
                PreparedStatement stockStmt = conn.prepareStatement("SELECT stock FROM products WHERE id = ?");
                stockStmt.setInt(1, productId);
                ResultSet stockRs = stockStmt.executeQuery();
                
                if (!stockRs.next() || stockRs.getInt("stock") < quantity) {
                    conn.rollback();
                    return false;
                }
            }
            
            // Create order
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            orderStmt.executeUpdate();
            
            // Get generated order ID
            ResultSet rs = orderStmt.getGeneratedKeys();
            if (!rs.next()) {
                conn.rollback();
                return false;
            }
            int orderId = rs.getInt(1);
            
            // Insert order items and update stock
            PreparedStatement itemsStmt = conn.prepareStatement(itemsSql);
            PreparedStatement updateStockStmt = conn.prepareStatement(updateStockSql);
            
            for (Map.Entry<Integer, Integer> item : orderData.getItems().entrySet()) {
                int productId = item.getKey();
                int quantity = item.getValue();
                
                // Update stock
                updateStockStmt.setInt(1, quantity);
                updateStockStmt.setInt(2, productId);
                updateStockStmt.setInt(3, quantity);
                int updatedRows = updateStockStmt.executeUpdate();
                
                if (updatedRows == 0) {
                    conn.rollback();
                    return false;
                }
                
                // Add order item - removed price parameter
                itemsStmt.setInt(1, orderId);
                itemsStmt.setInt(2, productId);
                itemsStmt.setInt(3, quantity);
                itemsStmt.executeUpdate();
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}