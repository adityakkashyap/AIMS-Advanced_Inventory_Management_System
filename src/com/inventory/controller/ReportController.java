package com.inventory.controller;

import com.inventory.InventoryFacade;
import com.inventory.ui.ReportView;

public class ReportController {
    private final InventoryFacade facade;
    private final ReportView view;

    public ReportController(InventoryFacade facade, ReportView view) {
        this.facade = facade;
        this.view = view;
    }

    public void generateReport(String reportType) {
        String result = facade.generateReport(reportType);
        view.displayReport(result);
        view.log("Generated " + reportType + " report.");
    }
}