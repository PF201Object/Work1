package classes;

import database.DatabaseUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

public class UserDialog extends JDialog {
    private JTextField txtUsername, txtFullName, txtEmail, txtPhone;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole, cmbGender;
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private String userId;  // Changed from Integer to String
    
    public UserDialog(Frame parent, String title, String userId) {  // Changed parameter type
        super(parent, title, true);
        this.userId = userId;
        initComponents();
        if (userId != null) {
            loadUserData();
        }
        setLocationRelativeTo(parent);
    }

    UserDialog(JFrame parentFrame, String update_User, int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(400, 350);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        panel.add(txtUsername, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        panel.add(txtPassword, gbc);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        txtFullName = new JTextField(20);
        panel.add(txtFullName, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        panel.add(txtEmail, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        txtPhone = new JTextField(20);
        panel.add(txtPhone, gbc);
        
        // Gender
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        cmbGender = new JComboBox<>(new String[]{"", "Male", "Female", "Other"});
        panel.add(cmbGender, gbc);
        
        // Role
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        cmbRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        panel.add(cmbRole, gbc);
        
        add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        
        btnSave.addActionListener(e -> saveUser());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadUserData() {
        try {
            String query = "SELECT * FROM users WHERE user_id = ?";
            ResultSet rs = DatabaseUtils.executeQuery(query, userId);
            if (rs.next()) {
                txtUsername.setText(rs.getString("username"));
                txtUsername.setEnabled(false);
                txtFullName.setText(rs.getString("full_name"));
                txtEmail.setText(rs.getString("email"));
                txtPhone.setText(rs.getString("phone"));
                cmbGender.setSelectedItem(rs.getString("gender"));
                cmbRole.setSelectedItem(rs.getString("role"));
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveUser() {
    if (txtUsername.getText().trim().isEmpty() || 
        txtFullName.getText().trim().isEmpty() || 
        txtEmail.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all required fields");
        return;
    }
    
    // Validate email format (basic validation)
    String email = txtEmail.getText().trim();
    if (!email.contains("@") || !email.contains(".")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid email address");
        return;
    }
    
    String query;
    if (userId == null || userId.isEmpty()) {
        // Check if email already exists before inserting
        if (isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email already exists! Please use a different email.");
            return;
        }
        query = "INSERT INTO users (username, password, full_name, email, phone, gender, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
    } else {
        // For update, check if email exists for OTHER users
        if (isEmailExistsForOtherUsers(email, userId)) {
            JOptionPane.showMessageDialog(this, "Email already exists! Please use a different email.");
            return;
        }
        query = "UPDATE users SET full_name=?, email=?, phone=?, gender=?, role=? WHERE user_id=?";
    }
    
    try {
        boolean success;
        if (userId == null || userId.isEmpty()) {
            success = DatabaseUtils.executeUpdate(query,
                txtUsername.getText(),
                new String(txtPassword.getPassword()),
                txtFullName.getText(),
                email,
                txtPhone.getText(),
                cmbGender.getSelectedItem(),
                cmbRole.getSelectedItem());
        } else {
            success = DatabaseUtils.executeUpdate(query,
                txtFullName.getText(),
                email,
                txtPhone.getText(),
                cmbGender.getSelectedItem(),
                cmbRole.getSelectedItem(),
                userId);
        }
        
        if (success) {
            saved = true;
            JOptionPane.showMessageDialog(this, "User saved successfully!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error saving user. Please check your input.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        // Check for specific SQLite constraint violation
        if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
            if (e.getMessage().contains("users.email")) {
                JOptionPane.showMessageDialog(this, "Email already exists! Please use a different email.");
            } else if (e.getMessage().contains("users.username")) {
                JOptionPane.showMessageDialog(this, "Username already exists! Please choose a different username.");
            } else {
                JOptionPane.showMessageDialog(this, "Duplicate entry! Please use different values.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}

// Helper method to check if email exists
private boolean isEmailExists(String email) {
    try {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        ResultSet rs = DatabaseUtils.executeQuery(query, email);
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}

// Helper method to check if email exists for other users (during update)
private boolean isEmailExistsForOtherUsers(String email, String currentUserId) {
    try {
        String query = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
        ResultSet rs = DatabaseUtils.executeQuery(query, email, currentUserId);
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
    
    public boolean isSaved() {
        return saved;
    }
}