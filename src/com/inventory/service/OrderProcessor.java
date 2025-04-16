package com.inventory.service;

import com.inventory.model.*;
import com.inventory.repository.ProductRepository;
import com.inventory.command.OrderCommand;
import com.inventory.command.UpdateInventoryCommand;

import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    private InventoryService inventoryService;
    private List<OrderCommand> commands;

    public OrderProcessor(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.commands = new ArrayList<>();
    }

    public boolean processOrder(OrderData orderData) {
        // First check if all products are available in the requested quantities
        if (!validateOrder(orderData)) {
            return false;
        }

        // Create commands for processing
        createCommands(orderData);

        // Execute all commands
        return executeCommands();
    }

    private boolean validateOrder(OrderData orderData) {
        for (OrderData.OrderItemData item : orderData.getItems()) {
            // Get product using InventoryService
            Product product = inventoryService.getProduct(item.getProductId());
            if (product == null || product.getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void createCommands(OrderData orderData) {
        commands.clear();

        // Create update inventory commands for each item (subtracting the quantity)
        for (OrderData.OrderItemData item : orderData.getItems()) {
            commands.add(new UpdateInventoryCommand(
                    inventoryService,
                    item.getProductId(),
                    -item.getQuantity()
            ));
        }
    }

    private boolean executeCommands() {
        // Execute all commands
        for (OrderCommand command : commands) {
            command.execute();
        }
        return true;
    }
}
