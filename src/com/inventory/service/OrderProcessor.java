package com.inventory.service;

import com.inventory.model.OrderData;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import java.util.Map;

public class OrderProcessor {
    private final ProductRepository productRepo;

    public OrderProcessor(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public boolean validateOrder(OrderData orderData) {
        // Check if order is empty
        if (orderData.getItems().isEmpty()) {
            return false;
        }

        // Validate each item
        for (Map.Entry<Integer, Integer> entry : orderData.getItems().entrySet()) {
            int productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            // Check for valid quantity
            if (requestedQuantity <= 0) {
                return false;
            }

            // Check product exists and has enough stock
            Product product = productRepo.findById(productId);
            if (product == null || product.getStock() < requestedQuantity) {
                return false;
            }
        }
        return true;
    }

    public boolean processOrder(OrderData orderData) {
        // Revalidate before processing
        if (!validateOrder(orderData)) {
            return false;
        }

        boolean success = true;
        // Process each item
        for (Map.Entry<Integer, Integer> entry : orderData.getItems().entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            
            // Decrease stock
            if (!productRepo.updateStock(productId, -quantity)) {
                success = false;
                break;
            }
        }

        return success;
    }
}