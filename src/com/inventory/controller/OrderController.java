package com.inventory.controller;
import com.inventory.model.OrderData;
import com.inventory.model.Product;
import com.inventory.repository.OrderRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.ui.OrderView;

import java.util.List;

public class OrderController {
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;
    private OrderView view; // Optional for UI updates

    public OrderController(ProductRepository productRepo, OrderRepository orderRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    public void setView(OrderView view) {
        this.view = view;
    }

    public boolean processOrder(OrderData orderData) {
        // Business logic for processing orders
        return orderRepo.createOrder(orderData);  // Changed from saveOrder to createOrder
    }

    public void refreshProductList() {
        if (view != null) {
            view.updateProductDropdown(productRepo.findAll());
        }
    }

    // UI-specific operations
    public void addToOrder(String productSelection, int quantity) {
        if (view != null) {
            if (productSelection == null || productSelection.isEmpty()) {
                view.showErrorMessage("Please select a product", "Invalid Selection");
                return;
            }

            try {
                int productId = Integer.parseInt(productSelection.split(" - ")[0]);
                Product product = productRepo.findById(productId);  // Changed from getProductDetails to findById

                if (product == null) {
                    view.showErrorMessage("Product not found", "Error");
                    return;
                }

                if (quantity > product.getStock()) {
                    view.showErrorMessage("Not enough stock available. Current stock: " + product.getStock(),
                            "Insufficient Stock");
                    return;
                }

                OrderData currentOrder = new OrderData();
                currentOrder.addItem(productId, quantity);
                view.addOrderItem(product, quantity);
                view.log("Added " + quantity + " x " + product.getDescription() + " to order.");
            } catch (Exception ex) {
                view.showErrorMessage("Error adding product to order: " + ex.getMessage(), "Error");
            }
        }
    }
    
    // Add missing completeOrder method
    public void completeOrder() {
        if (view != null) {
            OrderData orderData = view.getCurrentOrder();
            if (orderData != null && !orderData.getItems().isEmpty()) {
                boolean success = processOrder(orderData);
                if (success) {
                    view.clearOrder();
                    view.log("Order completed successfully");
                } else {
                    view.showErrorMessage("Failed to complete order", "Order Error");
                }
            } else {
                view.showErrorMessage("Cannot complete an empty order", "Empty Order");
            }
        }
    }
}