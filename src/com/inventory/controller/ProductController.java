package com.inventory.controller;

import com.inventory.InventoryFacade;
import com.inventory.model.Product;
import com.inventory.ui.ProductView;

import javax.swing.*;
import java.util.List;

public class ProductController {
    private final InventoryFacade facade;
    private final ProductView view;

    public ProductController(InventoryFacade facade, ProductView view) {
        this.facade = facade;
        this.view = view;
    }

    public void addProduct(String description, String priceStr, int stock) {
        if (description == null || description.trim().isEmpty()) {
            view.showErrorMessage("Please enter a product description", "Invalid Input");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                view.showErrorMessage("Price must be greater than 0", "Invalid Input");
                return;
            }

            boolean success = facade.addProduct(description.trim(), price, stock);
            if (success) {
                view.log("Added new product: " + description.trim());
                view.clearProductForm();
                refreshProducts();
            } else {
                view.log("Failed to add product");
            }
        } catch (NumberFormatException ex) {
            view.showErrorMessage("Please enter a valid price", "Invalid Input");
        }
    }

    public void updateStock(String productIdStr, int quantity) {
        try {
            int id = Integer.parseInt(productIdStr);
            
            boolean result = facade.updateStock(id, quantity);
            if (result) {
                view.log("Updated stock for product #" + id);
                view.clearStockUpdateForm();
                refreshProducts();
            } else {
                view.log("Failed to update stock for product #" + id);
                view.showErrorMessage("Failed to update stock. Check product ID and available stock.", "Update Failed");
            }
        } catch (NumberFormatException ex) {
            view.showErrorMessage("Please enter a valid product ID", "Invalid Input");
        }
    }

    public void refreshProducts() {
        List<Product> products = facade.getAllProducts();
        view.displayProducts(products);
    }
}