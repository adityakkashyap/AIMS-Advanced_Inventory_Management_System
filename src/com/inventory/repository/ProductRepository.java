package com.inventory.repository;

import com.inventory.model.Product;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository {
    private Map<Integer, Product> products;
    private DatabaseConnection dbConnection;

    public ProductRepository(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.products = new HashMap<>();
    }

    public Product findById(int id) {
        return products.get(id);
    }

    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    public void save(Product product) {
        products.put(product.getId(), product);
    }
}
