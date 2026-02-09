package swing;

import main.Main;
import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegistrationForm extends javax.swing.JPanel {
    private final Main dashboard;

    public RegistrationForm(Main parent) {
        this.dashboard = parent;
        initComponents();
        
        // Move customInit() code here
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(500, 500));
        
        btnRegister.setBackground(new Color(0, 153, 76));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorder(null);
        
        btnBack.setBackground(new Color(153, 153, 153));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setBorder(null);
        
        addButtonHoverEffect(btnRegister, new Color(0, 153, 76), new Color(0, 180, 90));
        addButtonHoverEffect(btnBack, new Color(153, 153, 153), new Color(180, 180, 180));
        
        this.setBackground(new Color(240, 248, 255));
    }
    
    private void addButtonHoverEffect(JButton button, Color baseColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
            }
        });
    }
    
private void handleRegistration() {
    String username = txtUsername.getText().trim();
    String password = new String(txtPassword.getPassword());
    String confirmPassword = new String(txtConfirmPassword.getPassword());
    String fullName = txtFullName.getText().trim();
    String email = txtEmail.getText().trim();
    String phone = txtPhone.getText().trim();
    
    // Basic validation - ALL fields required including phone
    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please fill all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Password validation
    if (!password.equals(confirmPassword)) {
        JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    if (password.length() < 6) {
        JOptionPane.showMessageDialog(this, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Username validation (no spaces, alphanumeric)
    if (username.contains(" ")) {
        JOptionPane.showMessageDialog(this, "Username cannot contain spaces!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    if (!username.matches("^[a-zA-Z0-9_]+$")) {
        JOptionPane.showMessageDialog(this, "Username can only contain letters, numbers, and underscores!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Email validation
    if (!email.contains("@") || !email.contains(".")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Email format validation (more strict)
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid email address format!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Phone validation (11 digits, Philippine format: 09XXXXXXXXX)
    // Remove any non-digit characters first
    phone = phone.replaceAll("[^0-9]", "");
    
    // Check if phone is empty after cleaning
    if (phone.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Phone number is required!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    if (phone.length() != 11) {
        JOptionPane.showMessageDialog(this, "Phone number must be 11 digits!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Check if starts with 09 (Philippine mobile)
    if (!phone.startsWith("09")) {
        JOptionPane.showMessageDialog(this, "Phone number must start with '09'!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Check if all digits are numbers
    if (!phone.matches("^[0-9]{11}$")) {
        JOptionPane.showMessageDialog(this, "Phone number must contain only digits!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Check for duplicate username AND email (separate checks to give specific messages)
    try (Connection con = DBConnection.connectDB()) {
        
        // Check for duplicate username
        try (PreparedStatement checkUsername = con.prepareStatement(
             "SELECT COUNT(*) FROM users WHERE username = ?")) {
            checkUsername.setString(1, username);
            ResultSet rsUsername = checkUsername.executeQuery();
            
            if (rsUsername.next() && rsUsername.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists! Please choose a different username.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Check for duplicate email
        try (PreparedStatement checkEmail = con.prepareStatement(
             "SELECT COUNT(*) FROM users WHERE email = ?")) {
            checkEmail.setString(1, email);
            ResultSet rsEmail = checkEmail.executeQuery();
            
            if (rsEmail.next() && rsEmail.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Email already registered! Please use a different email.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Check for duplicate phone
        try (PreparedStatement checkPhone = con.prepareStatement(
             "SELECT COUNT(*) FROM users WHERE phone = ?")) {
            checkPhone.setString(1, phone);
            ResultSet rsPhone = checkPhone.executeQuery();
            
            if (rsPhone.next() && rsPhone.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Phone number already registered!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // GENERATE NEW USER ID: Staff-1001, Staff-1002, etc.
        String newUserId = generateNewUserId(con);
        
        // Insert new user with generated User ID
        try (PreparedStatement insertStmt = con.prepareStatement(
            "INSERT INTO users (user_id, username, password, full_name, email, phone) VALUES (?, ?, ?, ?, ?, ?)")) {

            insertStmt.setString(1, newUserId);  // Use the generated string ID
            insertStmt.setString(2, username);
            insertStmt.setString(3, password);
            insertStmt.setString(4, fullName);
            insertStmt.setString(5, email);
            insertStmt.setString(6, phone);
            
            int rowsAffected = insertStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Registration successful!" + newUserId + "\nPlease login.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dashboard.showLoginForm();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

// Fix the generateNewUserId method to work with VARCHAR
private String generateNewUserId(Connection con) throws SQLException {
    try (PreparedStatement pst = con.prepareStatement(
         "SELECT user_id FROM users WHERE user_id LIKE 'Staff-%' ORDER BY LENGTH(user_id), user_id DESC LIMIT 1")) {
        
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            // Get the last user_id
            String lastUserId = rs.getString("user_id");
            System.out.println("Last user_id found: " + lastUserId);
            
            // Extract the number part (e.g., "Staff-1001" -> 1001)
            String numberPart = lastUserId.replace("Staff-", "");
            
            try {
                int lastNumber = Integer.parseInt(numberPart);
                // Increment and format back
                return String.format("Staff-%04d", lastNumber + 1);
            } catch (NumberFormatException e) {
                System.out.println("Error parsing number from: " + lastUserId);
                return "Staff-1001";  // Fallback
            }
        } else {
            System.out.println("No users found, starting with Staff-1001");
            return "Staff-1001";
        }
    }
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        txtConfirmPassword = new javax.swing.JPasswordField();
        btnRegister = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("User Registration");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 450, 60));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Username:");
        add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 70, -1, -1));

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 90, 250, 40));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Full Name:");
        add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 150, -1, -1));

        txtFullName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtFullName, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 170, 250, 40));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Email:");
        add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 230, -1, -1));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 250, 250, 40));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Phone:");
        add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 310, -1, -1));

        txtPhone.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 330, 250, 40));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Confirm Password:");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 150, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Password:");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 70, -1, -1));

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 90, 250, 40));

        txtConfirmPassword.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        add(txtConfirmPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 170, 250, 40));

        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRegister.setText("REGISTER");
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 230, 120, 40));

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBack.setText("BACK TO LOGIN");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 290, 150, 40));
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        handleRegistration();
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        dashboard.showLoginForm();
    }//GEN-LAST:event_btnBackActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPasswordField txtConfirmPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}