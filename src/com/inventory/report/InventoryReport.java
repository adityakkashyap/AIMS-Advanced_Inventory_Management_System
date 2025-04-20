package com.inventory.report;

import com.inventory.model.Product;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InventoryReport implements Report {
    
    @Override
    public String generate(List<Product> products) {
        StringBuilder report = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Header with current timestamp
        report.append("=== INVENTORY STATUS REPORT ===\n");
        report.append("Generated: ").append(sdf.format(new Date())).append("\n\n");
        
        // Summary section
        report.append("INVENTORY SUMMARY\n");
        report.append("----------------\n");
        report.append("Total Product Types: ").append(products.size()).append("\n");
        
        // Calculate totals
        int totalItems = 0;
        double totalValue = 0.0;
        int lowStockItems = 0;
        int outOfStockItems = 0;
        
        for (Product product : products) {
            totalItems += product.getStock();
            totalValue += (product.getPrice() * product.getStock());
            if (product.getStock() == 0) outOfStockItems++;
            else if (product.getStock() < 10) lowStockItems++;
        }
        
        report.append("Total Items in Stock: ").append(totalItems).append("\n");
        report.append("Total Inventory Value: $").append(String.format("%,.2f", totalValue)).append("\n");
        report.append("Low Stock Items (<10): ").append(lowStockItems).append("\n");
        report.append("Out of Stock Items: ").append(outOfStockItems).append("\n\n");
        
        // Detailed inventory listing
        report.append("DETAILED INVENTORY LISTING\n");
        report.append("-------------------------\n");
        report.append(String.format("%-5s %-30s %-10s %-10s %-15s %s\n", 
            "ID", "Description", "Price", "Stock", "Value", "Status"));
        report.append("--------------------------------------------------------------------------------\n");
        
        for (Product product : products) {
            double itemValue = product.getPrice() * product.getStock();
            String status = product.getStock() == 0 ? "OUT OF STOCK" :
                          product.getStock() < 10 ? "LOW STOCK" : "OK";
            
            report.append(String.format("%-5d %-30s $%-9.2f %-10d $%-14.2f %s\n",
                product.getId(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                itemValue,
                status
            ));
        }
        
        return report.toString();
    }
}