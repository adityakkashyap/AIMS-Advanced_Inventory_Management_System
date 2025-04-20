package com.inventory.report;

public class ReportFactory {
    public Report createReport(String reportType) {
        if (reportType == null) {
            return null;
        }
        
        switch (reportType.toLowerCase()) {
            case "inventory":
                return new InventoryReport();
            case "sales":
                return new SalesReport();
            default:
                return null;
        }
    }
}