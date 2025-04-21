package com.inventory;

import com.inventory.model.Product;
import com.inventory.report.InventoryReport;
import com.inventory.report.Report;
import com.inventory.report.SalesReport;
import com.inventory.model.OrderData;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.OrderRepository;
import com.inventory.service.NotificationService;
import com.inventory.service.OrderProcessor;
import com.inventory.report.ReportFactory;
import com.inventory.controller.*;
import java.util.List;

public class InventoryFacade {
    private final ProductController productController;
    private final OrderController orderController;
    private final ReportController reportController;
    private final NotificationService notificationService;

    public InventoryFacade() {
        ProductRepository productRepo = new ProductRepository();
        OrderRepository orderRepo = new OrderRepository();
        this.notificationService = new NotificationService();
        
        // Initialize controllers with repositories
        this.productController = new ProductController(productRepo);
        this.orderController = new OrderController(productRepo, orderRepo);
        this.reportController = new ReportController(productRepo);
    }

    // Getter methods for controllers
    public ProductController getProductController() {
        return productController;
    }

    public OrderController getOrderController() {
        return orderController;
    }

    public ReportController getReportController() {
        return reportController;
    }

    // Product operations delegated to ProductController
    public List<Product> getAllProducts() {
        return productController.getAllProducts();
    }

    public boolean updateStock(int productId, int quantity) {
        boolean success = productController.updateStock(productId, quantity);
        if (success) {
            notifyChange("Stock updated for product #" + productId);
        }
        return success;
    }

    public Product getProductDetails(int productId) {
        return productController.getProduct(productId);
    }

    public boolean addProduct(String description, double price, int stock) {
        boolean success = productController.addProduct(description, price, stock);
        if (success) {
            notifyChange("New product added: " + description);
        }
        return success;
    }

    // Order operations delegated to OrderController
    public boolean createOrder(OrderData orderData) {
        boolean success = orderController.processOrder(orderData);
        if (success) {
            notifyChange("New order created");
        }
        return success;
    }

    // Report operations delegated to ReportController
    public String generateReport(String reportType) {
        return reportController.generateReport(reportType);
    }

    // Observer pattern operations
    public void registerObserver(com.inventory.service.Observer observer) {
        notificationService.addObserver(observer);
    }

    public void removeObserver(com.inventory.service.Observer observer) {
        notificationService.removeObserver(observer);
    }

    private void notifyChange(String message) {
        notificationService.notifyObservers(message);
    }
}