package com.inventory.ui;

import com.inventory.InventoryFacade;
import com.inventory.controller.OrderController;
import com.inventory.controller.ProductController;
import com.inventory.controller.ReportController;
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
    
    // Controllers
    private ProductController productController;
    private OrderController orderController;
    private ReportController reportController;

    public InventoryManagementUI(InventoryFacade facade, UserRole role) {
        this.facade = facade;
        this.userRole = role;
        this.facade.registerObserver(this);
        
        initializeUI();
        
        // Initialize controllers after UI components are set up
        this.productController = new ProductController(facade, this);
        this.orderController = new OrderController(facade, this);
        this.reportController = new ReportController(facade, this);
        
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
        
        // Add Reports tab only for admin role
        if (userRole.canViewReports()) {
            tabbedPane.add("Reports", createReportPanel());
        }

        // Log area for system messages
        logArea = new JTextArea(6, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("System Log"));

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
            addButton.addActionListener(e -> 
                productController.addProduct(
                    addProductDescField.getText(), 
                    addProductPriceField.getText(), 
                    (Integer) addProductStockSpinner.getValue()
                )
            );
            
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
            updateButton.addActionListener(e -> 
                productController.updateStock(
                    updateStockIdField.getText(), 
                    (Integer) updateStockQuantitySpinner.getValue()
                )
            );
            
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
        refreshBtn.addActionListener(e -> productController.refreshProducts());
        
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
            orderController.addToOrder(
                (String) productSelector.getSelectedItem(),
                (Integer) quantitySpinner.getValue()
            )
        );

        // Complete order button
        JButton completeBtn = new JButton("Complete Order");
        
        // Use controller for complete order action
        completeBtn.addActionListener(e -> orderController.completeOrder());

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
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Report type selection
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> reportTypeBox = new JComboBox<>(new String[]{"inventory", "sales"});
        JButton generateBtn = new JButton("Generate");

        // Report area
        reportArea = new JTextArea(20, 60);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report"));

        // Use controller for generate report action
        generateBtn.addActionListener(e -> 
            reportController.generateReport((String) reportTypeBox.getSelectedItem())
        );

        // Layout components
        top.add(new JLabel("Report Type:"));
        top.add(reportTypeBox);
        top.add(generateBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

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
            productController.refreshProducts();
            if (userRole.canManageOrders()) {
                orderController.refreshProductList();
            }
        });
    }

    public void display() {
        frame.setVisible(true);
    }
}