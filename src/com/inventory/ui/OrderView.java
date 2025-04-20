package com.inventory.ui;

import com.inventory.model.Product;
import java.util.List;

public interface OrderView {
    void updateProductDropdown(List<Product> products);
    void addOrderItem(Product product, int quantity);
    void clearOrderItems();
    void showErrorMessage(String message, String title);
    void showInfoMessage(String message, String title);
    void log(String message);
}