package com.inventory.report;

public class ReportFactory {
    public Report createReport(String type) {
        if (type == null) {
            return null;
        }
        if (type.equalsIgnoreCase("sales")) {
            return new SalesReport();
        } else if (type.equalsIgnoreCase("inventory")) {
            return new InventoryReport();
        }
        return null;
    }
}
