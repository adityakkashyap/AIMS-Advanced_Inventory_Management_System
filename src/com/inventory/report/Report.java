package com.inventory.report;

import com.inventory.model.Product;
import java.util.List;

public interface Report {
    String generate(List<Product> products);
}