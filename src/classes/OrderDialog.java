package classes;

import database.DatabaseUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.util.List;
import java.util.Random;

public class OrderDialog extends JDialog {
    private JComboBox<String> cmbCustomer;
    private JTextField txtTotalAmount, txtReceivedAmount, txtChange, txtTrackingNumber;
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private String orderId;
    private List<String> customers;
    private JLabel lblChange, lblStatus;
    
    public OrderDialog(Frame parent, String title, String orderId) {
        super(parent, title, true);
        this.orderId = orderId;
        customers = DatabaseUtils.getCustomerNames();
        initComponents();
        if (orderId != null && !orderId.isEmpty()) {
            loadOrderData();
        } else {
            // For new orders, generate tracking number and set default status
            generateTrackingNumber();
        }
        setLocationRelativeTo(parent);
    }
    
    // Backward compatibility constructor
    public OrderDialog(Frame parent, String title, Integer orderId) {
        this(parent, title, orderId != null ? String.valueOf(orderId) : null);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(450, 400);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Customer
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Customer:*"), gbc);
        gbc.gridx = 1;
        cmbCustomer = new JComboBox<>(customers.toArray(new String[0]));
        panel.add(cmbCustomer, gbc);
        
        // Status - Display only (not editable)
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        lblStatus = new JLabel("PENDING");
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setForeground(new Color(255, 140, 0)); // Orange color
        panel.add(lblStatus, gbc);
        
        // Total Amount
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Total Amount:*"), gbc);
        gbc.gridx = 1;
        txtTotalAmount = new JTextField(20);
        panel.add(txtTotalAmount, gbc);
        
        // Received Amount
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Received Amount:*"), gbc);
        gbc.gridx = 1;
        txtReceivedAmount = new JTextField(20);
        panel.add(txtReceivedAmount, gbc);
        
        // Change (Auto-computed)
        gbc.gridx = 0; gbc.gridy = 4;
        lblChange = new JLabel("Change:");
        panel.add(lblChange, gbc);
        gbc.gridx = 1;
        txtChange = new JTextField(20);
        txtChange.setEditable(false);
        txtChange.setBackground(Color.LIGHT_GRAY);
        panel.add(txtChange, gbc);
        
        // Add listener to calculate change
        txtTotalAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateChange();
            }
        });
        txtReceivedAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateChange();
            }
        });
        
        // Tracking Number (Fixed format)
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Tracking Number:"), gbc);
        gbc.gridx = 1;
        txtTrackingNumber = new JTextField(20);
        txtTrackingNumber.setEditable(false);
        txtTrackingNumber.setBackground(Color.LIGHT_GRAY);
        panel.add(txtTrackingNumber, gbc);
        
        add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        
        btnSave.addActionListener(e -> saveOrder());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set required fields indicator
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        add(requiredLabel, BorderLayout.NORTH);
    }
    
    private void calculateChange() {
        try {
            double total = Double.parseDouble(txtTotalAmount.getText().trim());
            double received = Double.parseDouble(txtReceivedAmount.getText().trim());
            double change = received - total;
            
            if (change >= 0) {
                txtChange.setText(String.format("%.2f", change));
                txtChange.setForeground(Color.BLACK);
            } else {
                txtChange.setText(String.format("%.2f", change));
                txtChange.setForeground(Color.RED);
            }
        } catch (NumberFormatException e) {
            txtChange.setText("");
        }
    }
    
    private void generateTrackingNumber() {
        // Generate tracking number in format: TRK-1001, TRK-1002, etc.
        Random rand = new Random();
        int randomNum = 1001 + rand.nextInt(9000); // Generates 1001-9999
        txtTrackingNumber.setText("TRK-" + randomNum);
    }
    
    private void loadOrderData() {
        try {
            String query = "SELECT * FROM sales_orders WHERE order_id = ?";
            ResultSet rs = DatabaseUtils.executeQuery(query, orderId);
            if (rs.next()) {
                // Find and select customer
                int customerId = rs.getInt("customer_id");
                for (int i = 0; i < customers.size(); i++) {
                    if (customers.get(i).startsWith(customerId + " - ")) {
                        cmbCustomer.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Status is still PENDING (can't be changed by user)
                lblStatus.setText(rs.getString("order_status"));
                
                txtTotalAmount.setText(rs.getString("total_amount"));
                txtTrackingNumber.setText(rs.getString("tracking_number"));
                
                // Received amount and change would be loaded if they exist in your DB
                // If not, you might want to add these columns to your database
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveOrder() {
        // Validate required fields
        if (cmbCustomer.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer");
            return;
        }
        
        if (txtTotalAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter total amount");
            return;
        }
        
        if (txtReceivedAmount.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter received amount");
            return;
        }
        
        // Validate amounts
        double totalAmount, receivedAmount;
        try {
            totalAmount = Double.parseDouble(txtTotalAmount.getText().trim());
            receivedAmount = Double.parseDouble(txtReceivedAmount.getText().trim());
            
            if (totalAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Total amount must be greater than zero");
                return;
            }
            
            if (receivedAmount < totalAmount) {
                JOptionPane.showMessageDialog(this, "Received amount must be at least equal to total amount");
                return;
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for amounts");
            return;
        }
        
        int customerId = Integer.parseInt(((String)cmbCustomer.getSelectedItem()).split(" - ")[0]);
        
        // Status is always "Pending" for new orders
        String status = "Pending";
        String trackingNumber = txtTrackingNumber.getText().trim();
        
        String query;
        if (orderId == null || orderId.isEmpty()) {
            query = "INSERT INTO sales_orders (customer_id, order_status, total_amount, payment_method, tracking_number) VALUES (?, ?, ?, ?, ?)";
        } else {
            query = "UPDATE sales_orders SET customer_id=?, order_status=?, total_amount=?, payment_method=?, tracking_number=? WHERE order_id=?";
        }
        
        try {
            boolean success;
            if (orderId == null || orderId.isEmpty()) {
                success = DatabaseUtils.executeUpdate(query,
                    customerId,
                    status,
                    totalAmount,
                    "Cash", // Default payment method
                    trackingNumber);
            } else {
                success = DatabaseUtils.executeUpdate(query,
                    customerId,
                    status,
                    totalAmount,
                    "Cash", // Default payment method
                    trackingNumber,
                    orderId);
            }
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Order saved successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving order");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}