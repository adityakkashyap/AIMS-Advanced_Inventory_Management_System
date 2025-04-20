package com.inventory.command;

import com.inventory.service.InventoryService;

public class UpdateInventoryCommand implements Command {
    private final InventoryService inventoryService;
    private final int productId;
    private final int quantityChange;

    public UpdateInventoryCommand(InventoryService inventoryService, int productId, int quantityChange) {
        this.inventoryService = inventoryService;
        this.productId = productId;
        this.quantityChange = quantityChange;
    }

    @Override
    public void execute() {
        inventoryService.updateStock(productId, quantityChange);
    }
}