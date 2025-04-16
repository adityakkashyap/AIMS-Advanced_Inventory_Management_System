
package com.inventory.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private List<OrderItem> items;

    public Order(int orderId) {
        this.orderId = orderId;
        this.items = new ArrayList<>();
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public double calculateTotal() {
        return items.stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
    }

    public int getOrderId() {
        return orderId;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
