package com.inventory.report;

import java.util.Map;
import com.inventory.model.Product;
import java.util.List;

public class SalesReport implements Report {
    @Override
    public String generateReport(Map<String, Object> data) {
        StringBuilder report = new StringBuilder();
        report.append("===== SALES REPORT =====\n");

        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) data.get("products");

        double totalValue = 0;
        report.append("Product Sales Value:\n");

        for (Product product : products) {
            double productValue = product.getPrice() * product.getQuantity();
            totalValue += productValue;
            report.append(String.format("- %s: $%.2f ($%.2f x %d units)\n", 
                          product.getDescription(), productValue,
                          product.getPrice(), product.getQuantity()));
        }

        report.append(String.format("\nTotal Inventory Value: $%.2f\n", totalValue));
        report.append("========================\n");

        return report.toString();
    }
}
