package com.inventory.model;

public class Product {
    private final int id;
    private final String description;
    private final double price;
    private int stock;

    public Product(int id, String description, double price, int stock) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public int getId() { return id; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return String.format("Product #%d: %s (Price: $%.2f, Stock: %d)",
            id, description, price, stock);
    }
}