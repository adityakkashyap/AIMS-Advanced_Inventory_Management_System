package com.inventory;

import com.inventory.model.*;
import com.inventory.service.*;
import com.inventory.repository.*;
import com.inventory.report.*;

import java.util.*;

public class InventoryFacade {
    private ProductRepository productRepo;
    private InventoryService inventoryService;
    private OrderProcessor orderProcessor;
    private NotificationService notificationService;
    private ReportFactory reportFactory;

    public InventoryFacade() {
        // Initialize database connection
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();

        // Initialize repositories
        this.productRepo = new ProductRepository(dbConnection);

        // Initialize services
        this.notificationService = new NotificationService();
        this.inventoryService = new InventoryService(productRepo, notificationService);
        this.orderProcessor = new OrderProcessor(inventoryService);
        this.reportFactory = new ReportFactory();

        // Load initial inventory data
        initializeInventory();
    }

    private void initializeInventory() {
        // Add some sample products
        Product p1 = new Product(1, "Laptop", 999.99, 10);
        Product p2 = new Product(2, "Mouse", 24.99, 50);
        Product p3 = new Product(3, "Keyboard", 49.99, 30);

        productRepo.save(p1);
        productRepo.save(p2);
        productRepo.save(p3);
    }

    // Product related operations
    public Product getProductDetails(int id) {
        return productRepo.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Order related operations
    public boolean createOrder(OrderData orderData) {
        return orderProcessor.processOrder(orderData);
    }

    // Inventory management
    public boolean updateStock(int productId, int quantity) {
        return inventoryService.updateInventory(productId, quantity);
    }

    // Report generation
    public String generateReport(String type) {
        Report report = reportFactory.createReport(type);
        if (report != null) {
            return report.generateReport(getAllInventoryData());
        }
        return "Invalid report type";
    }

    // Get all inventory for reporting
    private Map<String, Object> getAllInventoryData() {
        Map<String, Object> data = new HashMap<>();
        data.put("products", productRepo.findAll());
        return data;
    }

    // Observer registration for notifications
    public void registerObserver(com.inventory.service.Observer observer) {
        notificationService.addObserver(observer);
    }
    
}
