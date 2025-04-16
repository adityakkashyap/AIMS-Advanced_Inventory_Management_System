package com.inventory;

import com.inventory.ui.InventoryManagementUI;

public class InventoryManagementApp {
    public static void main(String[] args) {
        // Initialize the system
        System.out.println("Initializing Inventory Management System...");

        // Create the facade that encapsulates the whole system
        InventoryFacade facade = new InventoryFacade();

        // Initialize the UI with the facade and display it
        InventoryManagementUI ui = new InventoryManagementUI(facade);
        ui.display();
    }
}
