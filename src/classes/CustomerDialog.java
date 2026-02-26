package classes;

import database.DatabaseUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

public class CustomerDialog extends JDialog {
    private JTextField txtFirstName, txtLastName, txtEmail, txtPhone, txtAddress;
    private JComboBox<String> cmbItem; // Combo box for item
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private String customerId;
    
    public CustomerDialog(Frame parent, String title, String customerId) {
        super(parent, title, true);
        this.customerId = customerId;
        initComponents();
        if (customerId != null && !customerId.isEmpty()) {
            loadCustomerData();
        }
        setLocationRelativeTo(parent);
    }
    
    // Backward compatibility constructor
    public CustomerDialog(Frame parent, String title, Integer customerId) {
        this(parent, title, customerId != null ? String.valueOf(customerId) : null);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(450, 400); // Adjusted size
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("First Name:*"), gbc);
        gbc.gridx = 1;
        txtFirstName = new JTextField(20);
        panel.add(txtFirstName, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Last Name:*"), gbc);
        gbc.gridx = 1;
        txtLastName = new JTextField(20);
        panel.add(txtLastName, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Phone:*"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(20);
        panel.add(txtPhone, gbc);
        
        // Item (Combo Box)
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Item Purchased:"), gbc);
        gbc.gridx = 1;
        String[] items = {
            "Running Shoes",
            "Basketball Shoes", 
            "Casual Sneakers",
            "Formal Leather Shoes",
            "Sandals"
        };
        cmbItem = new JComboBox<>(items);
        cmbItem.setSelectedIndex(0); // Select first item by default
        panel.add(cmbItem, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        txtAddress = new JTextField(20);
        panel.add(txtAddress, gbc);
        
        add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        
        btnSave.addActionListener(e -> saveCustomer());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set required fields indicator
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        add(requiredLabel, BorderLayout.NORTH);
    }
    
    private void loadCustomerData() {
        DatabaseUtils.executeQueryWithCallback(
            "SELECT * FROM customers WHERE customer_id = ?",
            rs -> {
                if (rs.next()) {
                    txtFirstName.setText(rs.getString("first_name"));
                    txtLastName.setText(rs.getString("last_name"));
                    txtEmail.setText(rs.getString("email"));
                    txtPhone.setText(rs.getString("phone_number"));
                    txtAddress.setText(rs.getString("default_shipping_address"));
                    
                    // Load item if exists
                    String item = rs.getString("item_purchased");
                    if (item != null && !item.isEmpty()) {
                        cmbItem.setSelectedItem(item);
                    }
                }
            },
            customerId
        );
    }
    
    private void saveCustomer() {
        // Validate required fields
        if (txtFirstName.getText().trim().isEmpty() || 
            txtLastName.getText().trim().isEmpty() || 
            txtEmail.getText().trim().isEmpty() ||
            txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields (*)");
            return;
        }
        
        // Validate email format
        String email = txtEmail.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address");
            return;
        }
        
        // Validate phone number (basic validation)
        String phone = txtPhone.getText().trim();
        if (!phone.matches("^[0-9+-]+$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number");
            return;
        }
        
        // Check if email already exists
        if (customerId == null || customerId.isEmpty()) {
            // For new customer
            if (isEmailExists(email)) {
                JOptionPane.showMessageDialog(this, 
                    "Email already exists! Please use a different email address.",
                    "Duplicate Email",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            // For updating customer
            if (isEmailExistsForOtherCustomers(email, customerId)) {
                JOptionPane.showMessageDialog(this, 
                    "Email already exists! Please use a different email address.",
                    "Duplicate Email",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Get selected item
        String selectedItem = (String) cmbItem.getSelectedItem();
        
        String query;
        if (customerId == null || customerId.isEmpty()) {
            query = "INSERT INTO customers (first_name, last_name, email, phone_number, item_purchased, default_shipping_address) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            query = "UPDATE customers SET first_name=?, last_name=?, email=?, phone_number=?, item_purchased=?, default_shipping_address=? WHERE customer_id=?";
        }
        
        try {
            boolean success;
            if (customerId == null || customerId.isEmpty()) {
                success = DatabaseUtils.executeUpdate(query,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    email,
                    phone,
                    selectedItem,
                    txtAddress.getText().trim());
            } else {
                success = DatabaseUtils.executeUpdate(query,
                    txtFirstName.getText().trim(),
                    txtLastName.getText().trim(),
                    email,
                    phone,
                    selectedItem,
                    txtAddress.getText().trim(),
                    customerId);
            }
            
            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Customer saved successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving customer. Please check your input.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Check for specific SQLite constraint violation
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
                if (e.getMessage().contains("customers.email")) {
                    JOptionPane.showMessageDialog(this, 
                        "Email already exists! Please use a different email address.",
                        "Duplicate Email",
                        JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Duplicate entry! Please use different values.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }
    
    private boolean isEmailExists(String email) {
        final boolean[] exists = {false};
        
        DatabaseUtils.executeQueryWithCallback(
            "SELECT COUNT(*) FROM customers WHERE email = ?",
            rs -> {
                if (rs.next()) {
                    exists[0] = rs.getInt(1) > 0;
                }
            },
            email
        );
        
        return exists[0];
    }
    
    private boolean isEmailExistsForOtherCustomers(String email, String currentCustomerId) {
        final boolean[] exists = {false};
        
        DatabaseUtils.executeQueryWithCallback(
            "SELECT COUNT(*) FROM customers WHERE email = ? AND customer_id != ?",
            rs -> {
                if (rs.next()) {
                    exists[0] = rs.getInt(1) > 0;
                }
            },
            email, currentCustomerId
        );
        
        return exists[0];
    }
    
    public boolean isSaved() {
        return saved;
    }
}