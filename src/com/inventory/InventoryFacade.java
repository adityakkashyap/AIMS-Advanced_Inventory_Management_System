package com.inventory;

import com.inventory.model.Product;
import com.inventory.report.InventoryReport;
import com.inventory.report.Report;
import com.inventory.report.SalesReport;
import com.inventory.model.OrderData;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.OrderRepository;
import com.inventory.service.NotificationService;
import com.inventory.report.ReportFactory;
import java.util.List;

public class InventoryFacade {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final NotificationService notificationService;
    private final ReportFactory reportFactory;

    public InventoryFacade() {
        this.productRepo = new ProductRepository();
        this.orderRepo = new OrderRepository();
        this.notificationService = new NotificationService();
        this.reportFactory = new ReportFactory();
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public boolean updateStock(int productId, int quantity) {
        boolean success = productRepo.updateStock(productId, quantity);
        if (success) {
            notificationService.notifyObservers("Stock updated for product #" + productId);
        }
        return success;
    }

    public boolean createOrder(OrderData orderData) {
        boolean success = orderRepo.createOrder(orderData);
        if (success) {
            notificationService.notifyObservers("New order created");
        }
        return success;
    }

    public Product getProductDetails(int productId) {
        return productRepo.findById(productId);
    }

    public void registerObserver(com.inventory.service.Observer observer) {
        notificationService.addObserver(observer);
    }

    public String generateReport(String reportType) {
        List<Product> products = getAllProducts();
        Report report = reportFactory.createReport(reportType);
        if (report != null) {
            return report.generate(products);
        }
        return "Unknown report type: " + reportType;
    }

    public boolean addProduct(String description, double price, int stock) {
        boolean success = productRepo.addProduct(description, price, stock);
        if (success) {
            notificationService.notifyObservers("New product added: " + description);
        }
        return success;
    }
}