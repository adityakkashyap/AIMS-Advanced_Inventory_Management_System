package com.inventory.controller;

import com.inventory.model.Product;
import com.inventory.repository.ProductRepository;
import com.inventory.ui.ProductView;

import javax.swing.*;
import java.util.List;

public class ProductController {
    private final ProductRepository productRepo;
    private ProductView view;

    public ProductController(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public void setView(ProductView view) {
        this.view = view;
    }

    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    public Product getProduct(int productId) {
        return productRepo.findById(productId);
    }

    public boolean addProduct(String description, double price, int stock) {
        return productRepo.addProduct(description, price, stock);
    }

    public boolean updateStock(int productId, int quantity) {
        return productRepo.updateStock(productId, quantity);
    }

    public void refreshProducts() {
        if (view != null) {
            view.displayProducts(getAllProducts());
        }
    }
}