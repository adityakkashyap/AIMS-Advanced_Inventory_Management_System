package com.inventory.ui;

import com.inventory.InventoryFacade;
import com.inventory.model.OrderData;
import com.inventory.model.Product;
import com.inventory.service.Observer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class InventoryManagementUI implements Observer {
    private final String userRole;
    private final InventoryFacade facade;
    private JFrame frame;
    private JTextArea logArea;
    private JComboBox<String> productSelector;
    private JSpinner quantitySpinner;
    // Declare the product list model at class level
    private DefaultListModel<String> productListModel;

    public InventoryManagementUI(InventoryFacade facade, String userRole) {
        this.facade = facade;
        this.userRole = userRole;
        this.facade.registerObserver(this);
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
    
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Always add Products tab for all roles
        tabbedPane.add("Products", createProductPanel());
        
        // Add Orders tab only for admin and sales roles
        if (userRole.equals("admin") || userRole.equals("sales")) {
            tabbedPane.add("Orders", createOrderPanel());
        }
        
        // Add Reports tab only for admin role
        if (userRole.equals("admin")) {
            tabbedPane.add("Reports", createReportPanel());
        }
    
        // Create log area
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
    
        // Initialize the class-level product list model
        productListModel = new DefaultListModel<>();
        JList<String> productList = new JList<>(productListModel);
        productList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(productList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Product List"));
    
        // Populate product list initially
        refreshProductList();
    
        // Create stock update form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField idField = new JTextField(5);
        JTextField qtyField = new JTextField(5);
        JButton updateBtn = new JButton("Update Stock");
    
        // Only admin and inventory can update stock
        if (userRole.equals("admin") || userRole.equals("inventory")) {
            updateBtn.addActionListener(e -> {
                try {
                    int id = Integer.parseInt(idField.getText());
                    int qty = Integer.parseInt(qtyField.getText());
                    boolean result = facade.updateStock(id, qty);
    
                    if (result) {
                        log("Updated stock for product #" + id);
                        refreshProductList();
                    } else {
                        log("Failed to update stock for product #" + id);
                    }
                } catch (NumberFormatException ex) {
                    log("Invalid input: " + ex.getMessage());
                }
            });
    
            form.add(new JLabel("Product ID:"));
            form.add(idField);
            form.add(new JLabel("Quantity to Add:"));
            form.add(qtyField);
            form.add(updateBtn);
        }
    
        // Create refresh button for manual refresh
        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> {
            refreshProductList();
            log("Product list refreshed.");
        });
    
        // Add Product Form (only for admin and inventory)
        if (userRole.equals("admin") || userRole.equals("inventory")) {
            JPanel addProductForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
            addProductForm.setBorder(BorderFactory.createTitledBorder("Add New Product"));
            
            JTextField descField = new JTextField(20);
            JTextField priceField = new JTextField(8);
            JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
            JButton addButton = new JButton("Add Product");
    
            addButton.addActionListener(e -> {
                try {
                    String description = descField.getText().trim();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = (Integer) stockSpinner.getValue();
    
                    if (description.isEmpty()) {
                        JOptionPane.showMessageDialog(frame,
                            "Please enter a product description",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
    
                    if (price <= 0) {
                        JOptionPane.showMessageDialog(frame,
                            "Price must be greater than 0",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
    
                    boolean success = facade.addProduct(description, price, stock);
                    if (success) {
                        log("Added new product: " + description);
                        descField.setText("");
                        priceField.setText("");
                        stockSpinner.setValue(0);
                        refreshProductList();
                    } else {
                        log("Failed to add product");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Please enter a valid price",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
    
            addProductForm.add(new JLabel("Description:"));
            addProductForm.add(descField);
            addProductForm.add(new JLabel("Price:"));
            addProductForm.add(priceField);
            addProductForm.add(new JLabel("Initial Stock:"));
            addProductForm.add(stockSpinner);
            addProductForm.add(addButton);
    
            // Add the form to the top of the panel
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.add(addProductForm, BorderLayout.NORTH);
            panel.add(topPanel, BorderLayout.NORTH);
        }
    
        // Panel to hold refresh button (aligned right)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controlPanel.add(refreshBtn);
    
        // Combine form and control panel at the bottom of product panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        if (userRole.equals("admin") || userRole.equals("inventory")) {
            bottomPanel.add(form, BorderLayout.CENTER);
        }
        bottomPanel.add(controlPanel, BorderLayout.EAST);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
    
        return panel;
    }

    // No-argument refresh method that updates the product list model
    private void refreshProductList() {
        productListModel.clear();
        List<Product> products = facade.getAllProducts();
        for (Product p : products) {
            productListModel.addElement(p.toString());
        }
    }

    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productSelector = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JButton addToOrderBtn = new JButton("Add to Order");

        DefaultListModel<String> orderItemsModel = new DefaultListModel<>();
        JList<String> orderItems = new JList<>(orderItemsModel);
        JScrollPane scrollPane = new JScrollPane(orderItems);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));

        OrderData orderData = new OrderData();

        // Populate product dropdown
        for (Product p : facade.getAllProducts()) {
            productSelector.addItem(p.getId() + " - " + p.getDescription());
        }

        addToOrderBtn.addActionListener(e -> {
            String selection = (String) productSelector.getSelectedItem();
            if (selection != null) {
                int productId = Integer.parseInt(selection.split(" - ")[0]);
                int quantity = (Integer) quantitySpinner.getValue();
                Product p = facade.getProductDetails(productId);
        
                if (p != null) {
                    if (quantity > p.getStock()) {
                        JOptionPane.showMessageDialog(frame,
                            "Not enough stock available. Current stock: " + p.getStock(),
                            "Insufficient Stock",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
        
                    orderData.addItem(productId, quantity);
                    orderItemsModel.addElement(String.format("%d x %s (Stock remaining: %d)",
                        quantity, p.getDescription(), p.getStock() - quantity));
                    log("Added " + quantity + " x " + p.getDescription() + " to order.");
                }
            }
        });

        JButton completeBtn = new JButton("Complete Order");
        completeBtn.addActionListener(e -> {
            if (orderItemsModel.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                    "Cannot complete empty order.",
                    "Empty Order",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        
            boolean result = facade.createOrder(orderData);
            if (result) {
                JOptionPane.showMessageDialog(frame,
                    "Order completed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                orderItemsModel.clear();
                orderData.getItems().clear();
                refreshProductList();
            } else {
                JOptionPane.showMessageDialog(frame,
                    "Order failed. Insufficient stock for one or more items.",
                    "Order Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

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

        JComboBox<String> reportTypeBox = new JComboBox<>(new String[]{"inventory", "sales"});
        JButton generateBtn = new JButton("Generate");

        JTextArea reportArea = new JTextArea(20, 60);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        reportArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report"));

        generateBtn.addActionListener(e -> {
            String type = (String) reportTypeBox.getSelectedItem();
            String result = facade.generateReport(type);
            reportArea.setText(result);
            log("Generated " + type + " report.");
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Report Type:"));
        top.add(reportTypeBox);
        top.add(generateBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
    }

    @Override
    public void update(String message) {
        SwingUtilities.invokeLater(() -> {
            log(message);
            // Optionally, refresh product list when a notification is received
            refreshProductList();
        });
    }

    public void display() {
        frame.setVisible(true);
    }
}



// package com.inventory.ui;

// import com.inventory.InventoryFacade;
// import com.inventory.model.OrderData;
// import com.inventory.model.Product;
// import com.inventory.service.Observer;

// import javax.swing.*;
// import javax.swing.border.EmptyBorder;
// import javax.swing.border.TitledBorder;
// import javax.swing.table.DefaultTableModel;
// import java.awt.*;
// import java.awt.event.KeyEvent;
// import java.text.NumberFormat;
// import java.util.List;

// public class InventoryManagementUI implements Observer {
//     private final InventoryFacade facade;
//     private JFrame frame;
//     private JTextArea logArea;
//     private JComboBox<String> productSelector;
//     private JSpinner quantitySpinner;
//     private JTable productTable;
//     private DefaultTableModel productTableModel;
//     private JTextField searchField;
//     private OrderData currentOrder;

//     // Colors
//     private final Color PRIMARY_COLOR = new Color(66, 139, 202);
//     private final Color ACCENT_COLOR = new Color(92, 184, 92);
//     private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
//     private final Color PANEL_BACKGROUND = new Color(255, 255, 255);
    
//     public InventoryManagementUI(InventoryFacade facade) {
//         this.facade = facade;
//         this.facade.registerObserver(this);
//         UIManager.put("TabbedPane.selected", PRIMARY_COLOR);
//         UIManager.put("Button.background", PRIMARY_COLOR);
//         UIManager.put("Button.foreground", Color.WHITE);
//         UIManager.put("TitledBorder.font", new Font("SansSerif", Font.BOLD, 12));
//         initializeUI();
//     }

//     private void initializeUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (Exception e) {
//             log("Could not set system look and feel: " + e.getMessage());
//         }

//         frame = new JFrame("Inventory Management System");
//         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         frame.setSize(1000, 700);
//         frame.setLocationRelativeTo(null);
//         frame.setLayout(new BorderLayout());
//         frame.getContentPane().setBackground(BACKGROUND_COLOR);

//         // Create the menu bar
//         JMenuBar menuBar = createMenuBar();
//         frame.setJMenuBar(menuBar);

//         JTabbedPane tabbedPane = new JTabbedPane();
//         tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
//         tabbedPane.addTab("Products", createIcon("product_icon.png", "Products"), createProductPanel());
//         tabbedPane.addTab("Orders", createIcon("order_icon.png", "Orders"), createOrderPanel());
//         tabbedPane.addTab("Reports", createIcon("report_icon.png", "Reports"), createReportPanel());
        
//         tabbedPane.setMnemonicAt(0, KeyEvent.VK_P);
//         tabbedPane.setMnemonicAt(1, KeyEvent.VK_O);
//         tabbedPane.setMnemonicAt(2, KeyEvent.VK_R);

//         logArea = new JTextArea(6, 60);
//         logArea.setEditable(false);
//         logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
//         logArea.setBackground(new Color(250, 250, 250));
//         JScrollPane logScrollPane = new JScrollPane(logArea);
//         TitledBorder logBorder = BorderFactory.createTitledBorder("System Log");
//         logBorder.setTitleColor(PRIMARY_COLOR);
//         logScrollPane.setBorder(logBorder);

//         // Status bar
//         JPanel statusBar = new JPanel(new BorderLayout());
//         statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
//         JLabel statusLabel = new JLabel("Ready");
//         statusBar.add(statusLabel, BorderLayout.WEST);

//         JPanel contentPanel = new JPanel(new BorderLayout());
//         contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
//         contentPanel.add(tabbedPane, BorderLayout.CENTER);
//         contentPanel.add(logScrollPane, BorderLayout.SOUTH);

//         frame.add(contentPanel, BorderLayout.CENTER);
//         frame.add(statusBar, BorderLayout.SOUTH);
//     }

//     private JMenuBar createMenuBar() {
//         JMenuBar menuBar = new JMenuBar();
        
//         JMenu fileMenu = new JMenu("File");
//         fileMenu.setMnemonic(KeyEvent.VK_F);
        
//         JMenuItem exportItem = new JMenuItem("Export Data...", KeyEvent.VK_E);
//         // Use standard menu shortcut key mask (compatible with older Java versions)
//         exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
//         exportItem.addActionListener(e -> log("Export functionality not implemented yet"));
        
//         JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
//         exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
//         exitItem.addActionListener(e -> System.exit(0));
        
//         fileMenu.add(exportItem);
//         fileMenu.addSeparator();
//         fileMenu.add(exitItem);
        
//         JMenu helpMenu = new JMenu("Help");
//         helpMenu.setMnemonic(KeyEvent.VK_H);
        
//         JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
//         aboutItem.addActionListener(e -> showAboutDialog());
        
//         helpMenu.add(aboutItem);
        
//         menuBar.add(fileMenu);
//         menuBar.add(helpMenu);
        
//         return menuBar;
//     }

//     private void showAboutDialog() {
//         JOptionPane.showMessageDialog(frame,
//             "Inventory Management System\nVersion 1.0\n\n" +
//             "A comprehensive solution for managing inventory, orders, and reports.",
//             "About Inventory Management System",
//             JOptionPane.INFORMATION_MESSAGE);
//     }

//     private Icon createIcon(String path, String fallback) {
//         // In a real app, you'd load actual icons
//         // For this example, we're returning null which will just show the text
//         return null;
//     }

//     private JPanel createProductPanel() {
//         JPanel panel = new JPanel(new BorderLayout(10, 10));
//         panel.setBorder(new EmptyBorder(10, 10, 10, 10));
//         panel.setBackground(PANEL_BACKGROUND);

//         // Search panel
//         JPanel searchPanel = new JPanel(new BorderLayout());
//         searchField = new JTextField(20);
//         searchField.setToolTipText("Enter product name or ID to search");
//         JButton searchButton = new JButton("Search");
//         searchButton.setToolTipText("Search for products");
        
//         searchButton.addActionListener(e -> performSearch());
//         searchField.addActionListener(e -> performSearch());
        
//         searchPanel.add(new JLabel("Search Products: "), BorderLayout.WEST);
//         searchPanel.add(searchField, BorderLayout.CENTER);
//         searchPanel.add(searchButton, BorderLayout.EAST);
        
//         // Product table
//         String[] columnNames = {"ID", "Description", "Price", "Stock", "Category"};
//         productTableModel = new DefaultTableModel(columnNames, 0) {
//             @Override
//             public boolean isCellEditable(int row, int column) {
//                 return false; // Make table read-only
//             }
//         };
        
//         productTable = new JTable(productTableModel);
//         productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//         productTable.setRowHeight(25);
//         productTable.getTableHeader().setReorderingAllowed(false);
//         productTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
//         JScrollPane tableScrollPane = new JScrollPane(productTable);
//         tableScrollPane.setBorder(createTitledBorder("Product Inventory"));
        
//         // Stock update form
//         JPanel form = new JPanel(new GridBagLayout());
//         form.setBorder(createTitledBorder("Update Stock"));
        
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(5, 5, 5, 5);
//         gbc.fill = GridBagConstraints.HORIZONTAL;
        
//         JTextField idField = new JTextField(10);
//         JSpinner qtyField = new JSpinner(new SpinnerNumberModel(1, -100, 100, 1));
//         JButton updateBtn = createStyledButton("Update Stock");
        
//         // Set up field validation
//         productTable.getSelectionModel().addListSelectionListener(e -> {
//             if (!e.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
//                 idField.setText(productTable.getValueAt(productTable.getSelectedRow(), 0).toString());
//             }
//         });

//         updateBtn.addActionListener(e -> {
//             try {
//                 int id = Integer.parseInt(idField.getText());
//                 int qty = (Integer) qtyField.getValue();
                
//                 // Confirm negative adjustments
//                 if (qty < 0) {
//                     int confirm = JOptionPane.showConfirmDialog(
//                         frame,
//                         "You are about to reduce stock by " + Math.abs(qty) + " units. Continue?",
//                         "Confirm Stock Reduction",
//                         JOptionPane.YES_NO_OPTION
//                     );
//                     if (confirm != JOptionPane.YES_OPTION) {
//                         return;
//                     }
//                 }
                
//                 boolean result = facade.updateStock(id, qty);

//                 if (result) {
//                     log("Updated stock for product #" + id + " by " + qty + " units");
//                     refreshProductTable();
//                     idField.setText("");
//                     qtyField.setValue(1);
//                 } else {
//                     JOptionPane.showMessageDialog(
//                         frame,
//                         "Failed to update stock for product #" + id,
//                         "Update Failed",
//                         JOptionPane.ERROR_MESSAGE
//                     );
//                     log("Failed to update stock for product #" + id);
//                 }
//             } catch (NumberFormatException ex) {
//                 JOptionPane.showMessageDialog(
//                     frame,
//                     "Please enter a valid product ID",
//                     "Invalid Input",
//                     JOptionPane.ERROR_MESSAGE
//                 );
//                 log("Invalid input: " + ex.getMessage());
//             }
//         });

//         // Layout form components
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         form.add(new JLabel("Product ID:"), gbc);

//         gbc.gridx = 1;
//         form.add(idField, gbc);

//         gbc.gridx = 0;
//         gbc.gridy = 1;
//         form.add(new JLabel("Quantity Change:"), gbc);

//         gbc.gridx = 1;
//         form.add(qtyField, gbc);

//         gbc.gridx = 0;
//         gbc.gridy = 2;
//         gbc.gridwidth = 2;
//         form.add(updateBtn, gbc);

//         // Main panel layout
//         JPanel topPanel = new JPanel(new BorderLayout(10, 0));
//         topPanel.add(searchPanel, BorderLayout.CENTER);
        
//         JPanel rightPanel = new JPanel(new BorderLayout());
//         rightPanel.add(form, BorderLayout.NORTH);
        
//         panel.add(topPanel, BorderLayout.NORTH);
//         panel.add(tableScrollPane, BorderLayout.CENTER);
//         panel.add(rightPanel, BorderLayout.EAST);

//         // Load initial data
//         refreshProductTable();

//         return panel;
//     }

//     private void performSearch() {
//         String searchTerm = searchField.getText().trim().toLowerCase();
//         if (searchTerm.isEmpty()) {
//             refreshProductTable();
//             return;
//         }
        
//         productTableModel.setRowCount(0);
        
//         for (Product p : facade.getAllProducts()) {
//             String productInfo = p.getId() + " " + p.getDescription().toLowerCase();
//             if (productInfo.contains(searchTerm)) {
//                 addProductToTable(p);
//             }
//         }
//     }

//     private JPanel createOrderPanel() {
//         JPanel panel = new JPanel(new BorderLayout(10, 10));
//         panel.setBorder(new EmptyBorder(10, 10, 10, 10));
//         panel.setBackground(PANEL_BACKGROUND);

//         // Initialize order
//         currentOrder = new OrderData();

//         // Product selection panel
//         JPanel selectionPanel = new JPanel(new GridBagLayout());
//         selectionPanel.setBorder(createTitledBorder("Add Product to Order"));
        
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(5, 5, 5, 5);
//         gbc.fill = GridBagConstraints.HORIZONTAL;
        
//         productSelector = new JComboBox<>();
//         productSelector.setToolTipText("Select a product to add to the order");
        
//         quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
//         quantitySpinner.setToolTipText("Specify quantity");
        
//         JButton addToOrderBtn = createStyledButton("Add to Order");
//         addToOrderBtn.setToolTipText("Add the selected product to the current order");

//         // Order items table
//         String[] columnNames = {"Product ID", "Description", "Quantity", "Unit Price", "Subtotal"};
//         DefaultTableModel orderItemsModel = new DefaultTableModel(columnNames, 0) {
//             @Override
//             public boolean isCellEditable(int row, int column) {
//                 return false;
//             }
//         };
        
//         JTable orderItemsTable = new JTable(orderItemsModel);
//         orderItemsTable.setRowHeight(25);
//         orderItemsTable.getTableHeader().setReorderingAllowed(false);
        
//         JScrollPane scrollPane = new JScrollPane(orderItemsTable);
//         scrollPane.setBorder(createTitledBorder("Order Items"));

//         // Populate dropdown
//         refreshProductComboBox();

//         // Action for adding products to order
//         addToOrderBtn.addActionListener(e -> {
//             String selection = (String) productSelector.getSelectedItem();
//             if (selection != null) {
//                 int productId = Integer.parseInt(selection.split(" - ")[0]);
//                 int quantity = (Integer) quantitySpinner.getValue();
//                 Product p = facade.getProductDetails(productId);

//                 if (p != null) {
//                     // Check if we have enough stock - using toString() to get the representation which will include stock info
//                     String productInfo = p.toString();
//                     int stockIndex = productInfo.indexOf("Stock: ");
//                     int stock = 0;
                    
//                     if (stockIndex >= 0) {
//                         // Extract stock value from the toString() representation
//                         // Assuming format like "Product #1: Description (Price: $10.00, Stock: 20)"
//                         String stockPart = productInfo.substring(stockIndex + 7);
//                         stock = Integer.parseInt(stockPart.split("\\D")[0]); // Get first number after "Stock: "
//                     }
                    
//                     if (stock < quantity) {
//                         JOptionPane.showMessageDialog(
//                             frame,
//                             "Not enough stock available. Current stock: " + stock,
//                             "Insufficient Stock",
//                             JOptionPane.WARNING_MESSAGE
//                         );
//                         return;
//                     }
                    
//                     currentOrder.addItem(productId, quantity);
                    
//                     // Add to order items table
//                     double price = 0.0;
//                     try {
//                         // Try to extract price from toString() if it contains price info
//                         int priceIndex = productInfo.indexOf("Price: $");
//                         if (priceIndex >= 0) {
//                             String pricePart = productInfo.substring(priceIndex + 8);
//                             price = Double.parseDouble(pricePart.split(",")[0]);
//                         }
//                     } catch (Exception ex) {
//                         // If we can't parse price, use a default
//                         price = 0.0;
//                     }
                    
//                     NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
//                     Object[] rowData = {
//                         productId,
//                         p.getDescription(),
//                         quantity,
//                         currencyFormat.format(price),
//                         currencyFormat.format(price * quantity)
//                     };
//                     orderItemsModel.addRow(rowData);
                    
//                     log("Added " + quantity + " x " + p.getDescription() + " to order.");
                    
//                     // Reset quantity spinner
//                     quantitySpinner.setValue(1);
//                 }
//             }
//         });

//         // Bottom panel with totals and action buttons
//         JPanel bottomPanel = new JPanel(new BorderLayout());
        
//         // Order summary panel
//         JPanel summaryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
//         summaryPanel.setBorder(createTitledBorder("Order Summary"));
        
//         JLabel totalItemsLabel = new JLabel("Total Items: 0");
//         JLabel totalValueLabel = new JLabel("Total Value: $0.00");
        
//         summaryPanel.add(totalItemsLabel);
//         summaryPanel.add(totalValueLabel);
        
//         // Action buttons
//         JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
//         JButton clearBtn = new JButton("Clear Order");
//         clearBtn.setBackground(new Color(217, 83, 79));
//         clearBtn.setForeground(Color.WHITE);
        
//         JButton completeBtn = createStyledButton("Complete Order");
//         completeBtn.setBackground(ACCENT_COLOR);
        
//         clearBtn.addActionListener(e -> {
//             if (orderItemsModel.getRowCount() > 0) {
//                 int confirm = JOptionPane.showConfirmDialog(
//                     frame,
//                     "Are you sure you want to clear the current order?",
//                     "Confirm Clear Order",
//                     JOptionPane.YES_NO_OPTION
//                 );
                
//                 if (confirm == JOptionPane.YES_OPTION) {
//                     orderItemsModel.setRowCount(0);
//                     currentOrder.getItems().clear();
//                     totalItemsLabel.setText("Total Items: 0");
//                     totalValueLabel.setText("Total Value: $0.00");
//                     log("Order cleared.");
//                 }
//             }
//         });

//         completeBtn.addActionListener(e -> {
//             if (orderItemsModel.getRowCount() == 0) {
//                 JOptionPane.showMessageDialog(
//                     frame,
//                     "Cannot complete an empty order.",
//                     "Empty Order",
//                     JOptionPane.WARNING_MESSAGE
//                 );
//                 return;
//             }

//             boolean result = facade.createOrder(currentOrder);
//             if (result) {
//                 JOptionPane.showMessageDialog(
//                     frame,
//                     "Order completed successfully!",
//                     "Order Complete",
//                     JOptionPane.INFORMATION_MESSAGE
//                 );
//                 log("Order completed successfully.");
                
//                 // Clear the order
//                 orderItemsModel.setRowCount(0);
//                 currentOrder = new OrderData();
//                 totalItemsLabel.setText("Total Items: 0");
//                 totalValueLabel.setText("Total Value: $0.00");
                
//                 // Refresh product list to show updated stock levels
//                 refreshProductTable();
//                 refreshProductComboBox();
//             } else {
//                 JOptionPane.showMessageDialog(
//                     frame,
//                     "Order failed. Please check stock levels.",
//                     "Order Failed",
//                     JOptionPane.ERROR_MESSAGE
//                 );
//                 log("Order failed. Check stock levels.");
//             }
//         });

//         actionPanel.add(clearBtn);
//         actionPanel.add(completeBtn);
        
//         bottomPanel.add(summaryPanel, BorderLayout.WEST);
//         bottomPanel.add(actionPanel, BorderLayout.EAST);

//         // Update summary when order changes
//         orderItemsTable.getModel().addTableModelListener(e -> {
//             int totalItems = 0;
//             double totalValue = 0.0;
            
//             for (int i = 0; i < orderItemsModel.getRowCount(); i++) {
//                 totalItems += (int) orderItemsModel.getValueAt(i, 2);
//                 String subtotalStr = orderItemsModel.getValueAt(i, 4).toString()
//                     .replace("$", "").replace(",", "");
//                 totalValue += Double.parseDouble(subtotalStr);
//             }
            
//             totalItemsLabel.setText("Total Items: " + totalItems);
//             totalValueLabel.setText("Total Value: " + NumberFormat.getCurrencyInstance().format(totalValue));
//         });

//         // Layout selection components
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         selectionPanel.add(new JLabel("Product:"), gbc);

//         gbc.gridx = 1;
//         gbc.weightx = 1.0;
//         selectionPanel.add(productSelector, gbc);

//         gbc.gridx = 2;
//         gbc.weightx = 0.0;
//         selectionPanel.add(new JLabel("Quantity:"), gbc);

//         gbc.gridx = 3;
//         selectionPanel.add(quantitySpinner, gbc);

//         gbc.gridx = 4;
//         selectionPanel.add(addToOrderBtn, gbc);

//         // Main panel layout
//         panel.add(selectionPanel, BorderLayout.NORTH);
//         panel.add(scrollPane, BorderLayout.CENTER);
//         panel.add(bottomPanel, BorderLayout.SOUTH);

//         return panel;
//     }

//     private JPanel createReportPanel() {
//         JPanel panel = new JPanel(new BorderLayout(10, 10));
//         panel.setBorder(new EmptyBorder(10, 10, 10, 10));
//         panel.setBackground(PANEL_BACKGROUND);

//         // Report options panel
//         JPanel optionsPanel = new JPanel(new GridBagLayout());
//         optionsPanel.setBorder(createTitledBorder("Report Options"));
        
//         GridBagConstraints gbc = new GridBagConstraints();
//         gbc.insets = new Insets(5, 5, 5, 5);
//         gbc.fill = GridBagConstraints.HORIZONTAL;
        
//         JComboBox<String> reportTypeBox = new JComboBox<>(new String[]{
//             "Inventory Status", 
//             "Sales Summary", 
//             "Low Stock Items",
//             "Product Performance"
//         });
//         reportTypeBox.setToolTipText("Select the type of report to generate");
        
//         JComboBox<String> timePeriodBox = new JComboBox<>(new String[]{
//             "Today",
//             "This Week",
//             "This Month",
//             "Last Month",
//             "This Year",
//             "All Time"
//         });
//         timePeriodBox.setToolTipText("Select the time period for the report");
        
//         JButton generateBtn = createStyledButton("Generate Report");
//         JButton exportBtn = new JButton("Export to PDF");
//         exportBtn.setEnabled(false);
        
//         // Report content area
//         JTextArea reportArea = new JTextArea(20, 60);
//         reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
//         reportArea.setEditable(false);
//         reportArea.setBackground(Color.WHITE);
        
//         JScrollPane scrollPane = new JScrollPane(reportArea);
//         scrollPane.setBorder(createTitledBorder("Report Output"));

//         generateBtn.addActionListener(e -> {
//             String type = reportTypeBox.getSelectedItem().toString().toLowerCase();
            
//             // Map UI-friendly names to backend report types
//             if (type.equals("inventory status")) type = "inventory";
//             else if (type.equals("sales summary")) type = "sales";
//             else if (type.equals("low stock items")) type = "lowstock";
//             else if (type.equals("product performance")) type = "performance";
            
//             String result = facade.generateReport(type);
//             reportArea.setText(result);
//             exportBtn.setEnabled(true);
//             log("Generated " + type + " report.");
//         });

//         exportBtn.addActionListener(e -> {
//             JFileChooser fileChooser = new JFileChooser();
//             fileChooser.setDialogTitle("Save Report");
//             if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
//                 log("Report export function not implemented yet");
//             }
//         });

//         // Layout options panel
//         gbc.gridx = 0;
//         gbc.gridy = 0;
//         optionsPanel.add(new JLabel("Report Type:"), gbc);

//         gbc.gridx = 1;
//         gbc.weightx = 1.0;
//         optionsPanel.add(reportTypeBox, gbc);

//         gbc.gridx = 2;
//         gbc.weightx = 0.0;
//         optionsPanel.add(new JLabel("Time Period:"), gbc);

//         gbc.gridx = 3;
//         gbc.weightx = 1.0;
//         optionsPanel.add(timePeriodBox, gbc);

//         gbc.gridx = 4;
//         gbc.weightx = 0.0;
//         optionsPanel.add(generateBtn, gbc);

//         gbc.gridx = 5;
//         optionsPanel.add(exportBtn, gbc);

//         // Main panel layout
//         panel.add(optionsPanel, BorderLayout.NORTH);
//         panel.add(scrollPane, BorderLayout.CENTER);

//         return panel;
//     }

//     private void refreshProductTable() {
//         productTableModel.setRowCount(0);
//         List<Product> products = facade.getAllProducts();
        
//         for (Product p : products) {
//             addProductToTable(p);
//         }
//     }
    
//     private void addProductToTable(Product p) {
//         // Extract stock and category from product's toString() implementation
//         // Assuming format like "Product #1: Description (Price: $10.00, Stock: 20, Category: Electronics)"
//         String productInfo = p.toString();
//         int stock = 0;
//         String category = "Uncategorized";
        
//         try {
//             // Extract stock
//             int stockIndex = productInfo.indexOf("Stock: ");
//             if (stockIndex >= 0) {
//                 String stockPart = productInfo.substring(stockIndex + 7);
//                 stock = Integer.parseInt(stockPart.split("\\D")[0]);
//             }
            
//             // Extract category
//             int categoryIndex = productInfo.indexOf("Category: ");
//             if (categoryIndex >= 0) {
//                 category = productInfo.substring(categoryIndex + 10).split("\\)")[0];
//             }
//         } catch (Exception e) {
//             // If parsing fails, use defaults
//             stock = 0;
//             category = "Uncategorized";
//         }
        
//         // Extract price
//         double price = 0.0;
//         try {
//             int priceIndex = productInfo.indexOf("Price: $");
//             if (priceIndex >= 0) {
//                 String pricePart = productInfo.substring(priceIndex + 8);
//                 price = Double.parseDouble(pricePart.split(",")[0]);
//             }
//         } catch (Exception e) {
//             price = 0.0;
//         }
        
//         NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
//         Object[] rowData = {
//             p.getId(),
//             p.getDescription(),
//             currencyFormat.format(price),
//             stock,
//             category
//         };
//         productTableModel.addRow(rowData);
//     }

//     private void refreshProductComboBox() {
//         productSelector.removeAllItems();
//         for (Product p : facade.getAllProducts()) {
//             // Extract stock from toString()
//             String productInfo = p.toString();
//             int stock = 0;
            
//             try {
//                 int stockIndex = productInfo.indexOf("Stock: ");
//                 if (stockIndex >= 0) {
//                     String stockPart = productInfo.substring(stockIndex + 7);
//                     stock = Integer.parseInt(stockPart.split("\\D")[0]);
//                 }
//             } catch (Exception e) {
//                 stock = 0;
//             }
            
//             if (stock > 0) {
//                 productSelector.addItem(p.getId() + " - " + p.getDescription());
//             }
//         }
//     }

//     private JButton createStyledButton(String text) {
//         JButton button = new JButton(text);
//         button.setBackground(PRIMARY_COLOR);
//         button.setForeground(Color.WHITE);
//         button.setFocusPainted(false);
//         return button;
//     }
    
//     private TitledBorder createTitledBorder(String title) {
//         TitledBorder border = BorderFactory.createTitledBorder(
//             BorderFactory.createLineBorder(new Color(200, 200, 200)),
//             title
//         );
//         border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
//         border.setTitleColor(PRIMARY_COLOR);
//         return border;
//     }

//     private void log(String msg) {
//         SwingUtilities.invokeLater(() -> {
//             logArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + msg + "\n");
//             // Auto-scroll to bottom
//             logArea.setCaretPosition(logArea.getDocument().getLength());
//         });
//     }

//     @Override
//     public void update(String message) {
//         SwingUtilities.invokeLater(() -> log(message));
//     }

//     public void display() {
//         frame.setVisible(true);
//     }
// }