package com.inventory.model;

import java.util.HashMap;
import java.util.Map;

public class OrderData {
    private final Map<Integer, Integer> items;

    public OrderData() {
        this.items = new HashMap<>();
    }

    public void addItem(int productId, int quantity) {
        items.put(productId, quantity);
    }

    public Map<Integer, Integer> getItems() {
        return items;
    }
}