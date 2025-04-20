package com.inventory.report;

import com.inventory.model.Product;
import java.util.List;

public class InventoryReport implements Report {
    @Override
    public String generate(List<Product> products) {
        StringBuilder report = new StringBuilder();
        report.append("Inventory Report\n");
        report.append("---------------\n\n");
        
        for (Product product : products) {
            report.append(String.format("Product: %s, Stock: %d, Price: $%.2f\n",
                product.getDescription(), product.getStock(), product.getPrice()));
        }
        
        int totalItems = products.stream().mapToInt(Product::getStock).sum();
        report.append(String.format("\nTotal Items in Stock: %d", totalItems));
        
        return report.toString();
    }
}