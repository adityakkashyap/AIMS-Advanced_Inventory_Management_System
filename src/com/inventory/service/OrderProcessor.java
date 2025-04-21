package com.inventory.service;

import com.inventory.model.OrderData;
import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.OrderRepository;
import java.util.Map;

public class OrderProcessor {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public OrderProcessor(ProductRepository productRepo, OrderRepository orderRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    public boolean validateOrder(OrderData orderData) {
        if (orderData == null || orderData.getItems().isEmpty()) {
            return false;
        }

        for (Map.Entry<Integer, Integer> entry : orderData.getItems().entrySet()) {
            int productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            if (requestedQuantity <= 0) {
                return false;
            }

            Product product = productRepo.findById(productId);
            if (product == null || product.getStock() < requestedQuantity) {
                return false;
            }
        }
        return true;
    }

    public boolean processOrder(OrderData orderData) {
        if (!validateOrder(orderData)) {
            return false;
        }

        // First update stock levels
        for (Map.Entry<Integer, Integer> entry : orderData.getItems().entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            
            if (!productRepo.updateStock(productId, -quantity)) {
                rollbackStockUpdates(orderData, entry.getKey());
                return false;
            }
        }

        // Then create order record
        return orderRepo.createOrder(orderData);
    }

    private void rollbackStockUpdates(OrderData orderData, int failedProductId) {
        for (Map.Entry<Integer, Integer> entry : orderData.getItems().entrySet()) {
            int productId = entry.getKey();
            if (productId == failedProductId) {
                break;
            }
            // Restore previous stock levels
            productRepo.updateStock(productId, entry.getValue());
        }
    }
}