package com.inventory;

import com.inventory.model.Product;
import com.inventory.report.InventoryReport;
import com.inventory.report.SalesReport;
import com.inventory.model.OrderData;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.OrderRepository;
import com.inventory.service.NotificationService;
import java.util.List;

public class InventoryFacade {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private final NotificationService notificationService;

    public InventoryFacade() {
        this.productRepo = new ProductRepository();
        this.orderRepo = new OrderRepository();
        this.notificationService = new NotificationService();
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

    // Add this method to the InventoryFacade class
    public String generateReport(String type) {
        switch (type.toLowerCase()) {
            case "inventory":
                return new InventoryReport().generate(getAllProducts());
            case "sales":
                return new SalesReport().generate(getAllProducts());
            default:
                return "Unknown report type: " + type;
        }
    }
}