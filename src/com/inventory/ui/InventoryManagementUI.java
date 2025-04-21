package com.inventory.ui;

import com.inventory.InventoryFacade;
import com.inventory.controller.OrderController;
import com.inventory.controller.ProductController;
import com.inventory.controller.ReportController;
import com.inventory.model.OrderData; // Add this import
import com.inventory.model.Product;
import com.inventory.model.UserRole;
import com.inventory.service.Observer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class InventoryManagementUI implements Observer, ProductView, OrderView, ReportView {
    // UI components
    private final UserRole userRole;
    private final InventoryFacade facade;
    private JFrame frame;
    private JTextArea logArea;
    private JComboBox<String> productSelector;
    private JSpinner quantitySpinner;
    private DefaultListModel<String> productListModel;
    private DefaultListModel<String> orderItemsModel;
    
    // Form fields
    private JTextField addProductDescField;
    private JTextField addProductPriceField;
    private JSpinner addProductStockSpinner;
    private JTextField updateStockIdField;
    private JSpinner updateStockQuantitySpinner;
    private JTextArea reportArea;

    public InventoryManagementUI(InventoryFacade facade, UserRole role) {
        this.facade = facade;
        this.userRole = role;
        this.facade.registerObserver(this);
        
        initializeUI();
        
        // Get controllers from facade and set views
        ProductController productController = facade.getProductController();
        OrderController orderController = facade.getOrderController();
        ReportController reportController = facade.getReportController();
        
        productController.setView(this);
        orderController.setView(this);
        reportController.setView(this);
        
        // Initial data load
        if (userRole.canManageProducts() || userRole.canManageOrders()) {
            productController.refreshProducts();
        }
        if (userRole.canManageOrders()) {
            orderController.refreshProductList();
        }
    }

    private void initializeUI() {
        // Frame setup
        frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Tabbed pane for different functionality
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Always add Products tab for all roles
        tabbedPane.add("Products", createProductPanel());
        
        // Add Orders tab only for admin and sales roles
        if (userRole.canManageOrders()) {
            tabbedPane.add("Orders", createOrderPanel());
        }
        
        // Add Reports tab based on role-specific report access
        // Check if user can access any report type instead of using deprecated canViewReports()
        if (userRole.canViewReport("sales") || userRole.canViewReport("inventory")) {
            tabbedPane.add("Reports", createReportPanel());
        }

        // Log area for system messages
        logArea = new JTextArea(6, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        
        frame.add(tabbedPane, BorderLayout.CENTER);
        frame.add(logScrollPane, BorderLayout.SOUTH);
    }

    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Product List
        productListModel = new DefaultListModel<>();
        JList<String> productList = new JList<>(productListModel);
        productList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Product List"));

        // Product management panels
        if (userRole.canManageProducts()) {
            JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 10));
            
            // Add product panel
            JPanel addProductPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            addProductPanel.setBorder(BorderFactory.createTitledBorder("Add New Product"));
            
            addProductDescField = new JTextField(20);
            addProductPriceField = new JTextField(8);
            addProductStockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
            JButton addButton = new JButton("Add Product");
            
            // Use controller for action
            addButton.addActionListener(e -> {
                try {
                    double price = Double.parseDouble(addProductPriceField.getText().trim());
                    facade.getProductController().addProduct(
                        addProductDescField.getText(), 
                        price, 
                        (Integer) addProductStockSpinner.getValue()
                    );
                } catch (NumberFormatException ex) {
                    showErrorMessage("Invalid price format", "Input Error");
                }
            });
            
            addProductPanel.add(new JLabel("Description:"));
            addProductPanel.add(addProductDescField);
            addProductPanel.add(new JLabel("Price:"));
            addProductPanel.add(addProductPriceField);
            addProductPanel.add(new JLabel("Initial Stock:"));
            addProductPanel.add(addProductStockSpinner);
            addProductPanel.add(addButton);
            
            // Update stock panel
            JPanel updateStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            updateStockPanel.setBorder(BorderFactory.createTitledBorder("Update Stock"));
            
            updateStockIdField = new JTextField(5);
            updateStockQuantitySpinner = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
            JButton updateButton = new JButton("Update Stock");
            
            // Use controller for action
            updateButton.addActionListener(e -> {
                try {
                    int productId = Integer.parseInt(updateStockIdField.getText().trim());
                    facade.getProductController().updateStock(
                        productId, 
                        (Integer) updateStockQuantitySpinner.getValue()
                    );
                } catch (NumberFormatException ex) {
                    showErrorMessage("Invalid product ID format", "Input Error");
                }
            });
            
            updateStockPanel.add(new JLabel("Product ID:"));
            updateStockPanel.add(updateStockIdField);
            updateStockPanel.add(new JLabel("Quantity Change:"));
            updateStockPanel.add(updateStockQuantitySpinner);
            updateStockPanel.add(updateButton);
            
            topPanel.add(addProductPanel);
            topPanel.add(updateStockPanel);
            panel.add(topPanel, BorderLayout.NORTH);
        }

        // Product list in center
        panel.add(scrollPane, BorderLayout.CENTER);

        // Refresh button at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        
        // Use controller for refresh
        refreshBtn.addActionListener(e -> facade.getProductController().refreshProducts());
        
        bottomPanel.add(refreshBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top panel for product selection
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productSelector = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton addToOrderBtn = new JButton("Add to Order");

        // Order items list
        orderItemsModel = new DefaultListModel<>();
        JList<String> orderItems = new JList<>(orderItemsModel);
        JScrollPane scrollPane = new JScrollPane(orderItems);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));

        // Use controller for add to order action
        addToOrderBtn.addActionListener(e -> 
            facade.getOrderController().addToOrder(
                (String) productSelector.getSelectedItem(),
                (Integer) quantitySpinner.getValue()
            )
        );

        // Complete order button
        JButton completeBtn = new JButton("Complete Order");
        
        // Use controller for complete order action
        completeBtn.addActionListener(e -> facade.getOrderController().completeOrder());

        // Layout components
        top.add(new JLabel("Product:"));
        top.add(productSelector);
        top.add(new JLabel("Quantity:"));
        top.add(quantitySpinner);
        top.add(addToOrderBtn);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(completeBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Control panel for report options
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        // Use exact strings that match UserRole.canViewReport() checks
        String[] reportTypes = {"sales", "inventory"};
        JComboBox<String> reportTypeBox = new JComboBox<>(reportTypes);
        
        // Filter report types based on user role
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String type : reportTypes) {
            if (userRole.canViewReport(type)) {
                model.addElement(type);
            }
        }
        reportTypeBox.setModel(model);
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            String reportType = (String) reportTypeBox.getSelectedItem();
            if (reportType != null) {
                String reportContent = facade.getReportController().generateReport(reportType);
                displayReport(reportContent);
                log("Generated " + reportType + " report");
            }
        });
        
        controlPanel.add(reportTypeBox);
        controlPanel.add(generateBtn);
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Report display area 
        reportArea = new JTextArea(20, 50);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        
        return panel;
    }

    // ProductView implementation
    @Override
    public void displayProducts(List<Product> products) {
        productListModel.clear();
        for (Product p : products) {
            productListModel.addElement(p.toString());
        }
    }

    @Override
    public void clearProductForm() {
        if (addProductDescField != null) {
            addProductDescField.setText("");
        }
        if (addProductPriceField != null) {
            addProductPriceField.setText("");
        }
        if (addProductStockSpinner != null) {
            addProductStockSpinner.setValue(0);
        }
    }

    @Override
    public void clearStockUpdateForm() {
        if (updateStockIdField != null) {
            updateStockIdField.setText("");
        }
        if (updateStockQuantitySpinner != null) {
            updateStockQuantitySpinner.setValue(0);
        }
    }

    // OrderView implementation
    @Override
    public void updateProductDropdown(List<Product> products) {
        if (productSelector != null) {
            productSelector.removeAllItems();
            for (Product p : products) {
                if (p.getStock() > 0) {
                    productSelector.addItem(p.getId() + " - " + p.getDescription());
                }
            }
        }
    }

    @Override
    public void addOrderItem(Product product, int quantity) {
        if (orderItemsModel != null) {
            orderItemsModel.addElement(String.format("%d x %s (Stock remaining: %d)",
                quantity, product.getDescription(), product.getStock() - quantity));
        }
    }

    @Override
    public void clearOrderItems() {
        if (orderItemsModel != null) {
            orderItemsModel.clear();
        }
    }

    @Override
    public OrderData getCurrentOrder() {
        OrderData orderData = new OrderData();
        // Parse each item in the order list model to extract product ID and quantity
        for (int i = 0; i < orderItemsModel.size(); i++) {
            String item = orderItemsModel.get(i);
            try {
                // Format is: "X x Product Description (Stock remaining: Y)"
                String[] parts = item.split(" x ", 2);
                int quantity = Integer.parseInt(parts[0]);
                
                // Get product ID from the selected item in productSelector
                String selectedItem = (String) productSelector.getSelectedItem();
                if (selectedItem != null && !selectedItem.isEmpty()) {
                    int productId = Integer.parseInt(selectedItem.split(" - ")[0]);
                    orderData.addItem(productId, quantity);
                }
            } catch (Exception e) {
                // Skip malformed items
                log("Error parsing order item: " + item);
            }
        }
        return orderData;
    }

    @Override
    public void clearOrder() {
        clearOrderItems();
        if (quantitySpinner != null) {
            quantitySpinner.setValue(1);
        }
    }

    // ReportView implementation
    @Override
    public void displayReport(String reportContent) {
        if (reportArea != null) {
            reportArea.setText(reportContent);
        }
    }

    // Common message methods
    @Override
    public void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfoMessage(String message, String title) {
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void log(String message) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
        }
    }

    // Observer implementation
    @Override
    public void update(String message) {
        SwingUtilities.invokeLater(() -> {
            log(message);
            facade.getProductController().refreshProducts();
            if (userRole.canManageOrders()) {
                facade.getOrderController().refreshProductList();
            }
        });
    }

    public void display() {
        frame.setVisible(true);
    }
}