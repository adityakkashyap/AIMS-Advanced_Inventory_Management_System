package com.inventory.ui;

import com.inventory.model.Product;
import java.util.List;

public interface ProductView {
    void displayProducts(List<Product> products);
    void clearProductForm();
    void clearStockUpdateForm();
    void showErrorMessage(String message, String title);
    void log(String message);
}