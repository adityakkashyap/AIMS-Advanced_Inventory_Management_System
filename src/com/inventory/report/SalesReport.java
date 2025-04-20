package com.inventory.report;

import com.inventory.model.Product;
import com.inventory.repository.OrderRepository;
import java.util.*;
import java.text.SimpleDateFormat;

public class SalesReport implements Report {
    private final OrderRepository orderRepo;
    
    public SalesReport() {
        this.orderRepo = new OrderRepository();
    }
    
    @Override
    public String generate(List<Product> products) {
        StringBuilder report = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // Header
        report.append("=== SALES PERFORMANCE REPORT ===\n");
        report.append("Generated: ").append(sdf.format(new Date())).append("\n\n");
        
        Map<String, Map<String, Object>> salesData = orderRepo.getDetailedSalesData();
        
        // Sales Summary
        double totalRevenue = 0.0;
        int totalUnitsSold = 0;
        Set<String> uniqueCustomers = new HashSet<>();
        
        for (Map.Entry<String, Map<String, Object>> entry : salesData.entrySet()) {
            Map<String, Object> data = entry.getValue();
            totalRevenue += (Double) data.get("revenue");
            totalUnitsSold += (Integer) data.get("quantity");
            uniqueCustomers.add((String) data.get("customer"));
        }
        
        report.append("SALES SUMMARY\n");
        report.append("-------------\n");
        report.append("Total Revenue: $").append(String.format("%,.2f", totalRevenue)).append("\n");
        report.append("Total Units Sold: ").append(totalUnitsSold).append("\n");
        report.append("Unique Customers: ").append(uniqueCustomers.size()).append("\n");
        report.append("Average Order Value: $")
              .append(String.format("%,.2f", totalRevenue / uniqueCustomers.size()))
              .append("\n\n");
        
        // Top Selling Products
        report.append("TOP SELLING PRODUCTS\n");
        report.append("-------------------\n");
        report.append(String.format("%-30s %-12s %-12s %s\n", 
            "Product", "Units Sold", "Revenue", "% of Total"));
        report.append("----------------------------------------------------------\n");
        
        // Sort products by revenue
        List<Map.Entry<String, Map<String, Object>>> sortedEntries = 
            new ArrayList<>(salesData.entrySet());
        sortedEntries.sort((e1, e2) -> {
            Double rev2 = (Double) e2.getValue().get("revenue");
            Double rev1 = (Double) e1.getValue().get("revenue");
            return rev2.compareTo(rev1);
        });
        
        // Show top 5 products
        int count = 0;
        for (Map.Entry<String, Map<String, Object>> entry : sortedEntries) {
            if (count++ >= 5) break;
            
            Map<String, Object> data = entry.getValue();
            double revenue = (Double) data.get("revenue");
            int quantity = (Integer) data.get("quantity");
            double percentage = (revenue / totalRevenue) * 100;
            
            report.append(String.format("%-30s %-12d $%-11.2f %.1f%%\n",
                entry.getKey(),
                quantity,
                revenue,
                percentage
            ));
        }
        
        // Monthly Trend
        report.append("\nMONTHLY SALES TREND\n");
        report.append("-------------------\n");
        Map<String, Double> monthlyTrend = orderRepo.getMonthlyTrend();
        for (Map.Entry<String, Double> entry : monthlyTrend.entrySet()) {
            report.append(String.format("%-10s $%,12.2f\n", 
                entry.getKey(), entry.getValue()));
        }
        
        return report.toString();
    }
}