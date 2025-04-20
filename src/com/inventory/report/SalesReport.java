package com.inventory.report;

import com.inventory.model.Product;
import java.util.List;

public class SalesReport implements Report {
    @Override
    public String generate(List<Product> products) {
        StringBuilder report = new StringBuilder();
        report.append("Sales Value Report\n");
        report.append("----------------\n\n");
        
        double totalValue = 0.0;
        for (Product product : products) {
            double productValue = product.getPrice() * product.getStock();
            totalValue += productValue;
            
            report.append(String.format("Product: %s, Unit Price: $%.2f, Stock: %d\n",
                product.getDescription(), product.getPrice(), product.getStock()));
        }
        
        report.append(String.format("\nTotal Inventory Value: $%.2f", totalValue));
        
        return report.toString();
    }
}