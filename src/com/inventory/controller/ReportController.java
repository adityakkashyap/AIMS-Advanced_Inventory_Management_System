package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.report.ReportFactory;
import com.inventory.report.Report;
import com.inventory.repository.ProductRepository;
import com.inventory.ui.ReportView;

import java.util.List;

public class ReportController {
    private final ProductRepository productRepo;
    private final ReportFactory reportFactory;
    private ReportView view;  // Added missing field

    public ReportController(ProductRepository productRepo) {
        this.productRepo = productRepo;
        this.reportFactory = new ReportFactory();
    }

    public void setView(ReportView view) {
        this.view = view;
    }

    public String generateReport(String reportType) {
        List<Product> products = productRepo.findAll();
        Report report = reportFactory.createReport(reportType);
        return report != null ? report.generate(products) : "Unknown report type: " + reportType;
    }
}