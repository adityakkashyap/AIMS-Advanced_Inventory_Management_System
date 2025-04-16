package com.inventory.report;

import java.util.Map;
import com.inventory.model.Product;
import java.util.List;

public class InventoryReport implements Report {
    @Override
    public String generateReport(Map<String, Object> data) {
        StringBuilder report = new StringBuilder();
        report.append("===== INVENTORY REPORT =====\n");

        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) data.get("products");

        report.append("Current Inventory Status:\n");

        for (Product product : products) {
            report.append(String.format("- %s: %d units in stock ($%.2f each)\n", 
                          product.getDescription(), product.getQuantity(), product.getPrice()));
        }

        int totalItems = products.stream().mapToInt(Product::getQuantity).sum();
        report.append(String.format("\nTotal Items in Inventory: %d\n", totalItems));
        report.append("============================\n");

        return report.toString();
    }
}
