package com.inventory.command;

import com.inventory.service.InventoryService;

public class UpdateInventoryCommand implements OrderCommand {
    private InventoryService inventoryService;
    private int productId;
    private int quantityChange;

    public UpdateInventoryCommand(InventoryService inventoryService, int productId, int quantityChange) {
        this.inventoryService = inventoryService;
        this.productId = productId;
        this.quantityChange = quantityChange;
    }

    @Override
    public void execute() {
        inventoryService.updateInventory(productId, quantityChange);
    }
}
