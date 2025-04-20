package com.inventory.repository;

import com.inventory.db.DatabaseConnection;
import com.inventory.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM products";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
                
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean save(Product product) {
        String query = "INSERT INTO products (description, price, stock) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
                
            stmt.setString(1, product.getDescription());
            stmt.setDouble(2, product.getPrice());
            stmt.setInt(3, product.getStock());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Product findById(int productId) {
        String query = "SELECT * FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
                
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
                
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getInt("stock")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updateStock(int productId, int quantityChange) {
        String query = "UPDATE products SET stock = stock + ? " +
                      "WHERE id = ? AND (stock + ?) >= 0";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
                
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantityChange);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}