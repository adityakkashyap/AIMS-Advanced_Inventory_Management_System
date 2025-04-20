package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;

public class InventoryService {
    private final ProductRepository productRepo;
    private final NotificationService notificationService;

    public InventoryService(ProductRepository productRepo, NotificationService notificationService) {
        this.productRepo = productRepo;
        this.notificationService = notificationService;
    }

    public boolean updateStock(int productId, int quantityChange) {
        Product product = productRepo.findById(productId);
        if (product == null) {
            return false;
        }

        int newQuantity = product.getStock() + quantityChange;
        if (newQuantity < 0) {
            return false;
        }

        product.setStock(newQuantity);
        notificationService.notifyObservers("Stock updated for product #" + productId);
        return true;
    }
}