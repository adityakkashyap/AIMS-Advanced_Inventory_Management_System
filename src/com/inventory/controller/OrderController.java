package com.inventory.controller;

import com.inventory.InventoryFacade;
import com.inventory.model.OrderData;
import com.inventory.model.Product;
import com.inventory.ui.OrderView;

import java.util.List;

public class OrderController {
    private final InventoryFacade facade;
    private final OrderView view;
    private final OrderData currentOrder;

    public OrderController(InventoryFacade facade, OrderView view) {
        this.facade = facade;
        this.view = view;
        this.currentOrder = new OrderData();
    }

    public void addToOrder(String productSelection, int quantity) {
        if (productSelection == null || productSelection.isEmpty()) {
            view.showErrorMessage("Please select a product", "Invalid Selection");
            return;
        }

        try {
            int productId = Integer.parseInt(productSelection.split(" - ")[0]);
            Product product = facade.getProductDetails(productId);

            if (product == null) {
                view.showErrorMessage("Product not found", "Error");
                return;
            }

            if (quantity > product.getStock()) {
                view.showErrorMessage("Not enough stock available. Current stock: " + product.getStock(), 
                                    "Insufficient Stock");
                return;
            }

            currentOrder.addItem(productId, quantity);
            view.addOrderItem(product, quantity);
            view.log("Added " + quantity + " x " + product.getDescription() + " to order.");
        } catch (Exception ex) {
            view.showErrorMessage("Error adding product to order: " + ex.getMessage(), "Error");
        }
    }

    public void completeOrder() {
        if (currentOrder.getItems().isEmpty()) {
            view.showErrorMessage("Cannot complete empty order", "Empty Order");
            return;
        }

        boolean result = facade.createOrder(currentOrder);
        if (result) {
            view.showInfoMessage("Order completed successfully!", "Success");
            view.log("Order completed successfully.");
            currentOrder.getItems().clear();
            view.clearOrderItems();
            refreshProductList();
        } else {
            view.showErrorMessage("Order failed. Check stock levels.", "Order Failed");
            view.log("Order failed. Check stock levels.");
        }
    }

    public void refreshProductList() {
        List<Product> products = facade.getAllProducts();
        view.updateProductDropdown(products);
    }
}