package com.inventory.model;

import java.util.ArrayList;
import java.util.List;

public class OrderData {
    private List<OrderItemData> items;

    public OrderData() {
        this.items = new ArrayList<>();
    }

    public void addItem(int productId, int quantity) {
        items.add(new OrderItemData(productId, quantity));
    }

    public List<OrderItemData> getItems() {
        return items;
    }

    // Inner class representing individual order items
    public static class OrderItemData {
        private int productId;
        private int quantity;

        public OrderItemData(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public int getProductId() {
            return productId;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
