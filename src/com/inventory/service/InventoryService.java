package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;

public class InventoryService {
    private ProductRepository repository;
    private NotificationService notificationService;

    public InventoryService(ProductRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public boolean updateInventory(int productId, int quantityChange) {
        Product product = repository.findById(productId);
        if (product == null) {
            return false;
        }

        int newQuantity = product.getQuantity() + quantityChange;
        if (newQuantity < 0) {
            return false;
        }

        product.setQuantity(newQuantity);
        repository.save(product);

        // Notify observers about inventory change
        notificationService.notifyObservers("Product " + product.getDescription() +
                " inventory updated to " + newQuantity);

        // Alert if stock is low (less than 5)
        if (newQuantity < 5) {
            notificationService.notifyObservers("LOW STOCK ALERT: " + product.getDescription() +
                    " has only " + newQuantity + " remaining!");
        }

        return true;
    }

    public Product getProduct(int productId) {
        return repository.findById(productId);
    }
}
