package swing;

import main.Main;
import database.DBConnection;
import javax.swing.*;
import java.sql.*;

public class ProfilePanel extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    
    public ProfilePanel(Main parent, String username) {
        this.dashboard = parent;
        this.username = username;
        initComponents();
        loadProfileDataSafe();
    }
    
private void loadProfileDataSafe() {
    System.out.println("Loading profile for: " + username);
    
    try (Connection con = DBConnection.connectDB()) {
        String query = "SELECT user_id, username, full_name, email, phone, role, gender, " +
                      "SUBSTR(created_at, 1, 10) as join_date " +
                      "FROM users WHERE username = ?";
        
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                System.out.println("User found!");
                
                // Format user_id as Staff-1001, etc.
                int userId = rs.getInt("user_id");
                String displayId;
                
                // Adjust for Staff-1001 format
                if (userId == 0) {
                    // If first ID is 0, show as Staff-1001
                    displayId = "Staff-1001";
                } else if (userId < 1000) {
                    // If ID is less than 1000, add 1000
                    displayId = String.format("Staff-%04d", userId + 1000);
                } else {
                    // If ID is already 1000+, use as is
                    displayId = String.format("Staff-%04d", userId);
                }
                
                // Load text fields
                txtFullName.setText(rs.getString("full_name"));
                txtEmail.setText(rs.getString("email"));
                txtPhone.setText(rs.getString("phone"));
                
                // Gender
                String gender = rs.getString("gender");
                if (gender != null && !gender.isEmpty()) {
                    cmbGender.setSelectedItem(gender);
                }
                
                // Labels with formatted user_id
                lblRole.setText("Role: " + rs.getString("role"));
                lblUserId.setText("User ID: " + displayId); 
                User.setText("Username: " + rs.getString("username"));
                FN.setText("Full Name: " + rs.getString("full_name"));
                EM.setText("Email: " + rs.getString("email"));
                
                // Format phone display
                String phone = rs.getString("phone");
                if (phone != null && !phone.isEmpty()) {
                    Con.setText("Contacts: (+63) - " + phone);
                } else {
                    Con.setText("Contacts: Not provided");
                }
            } else {
                JOptionPane.showMessageDialog(this, "User not found in database!");
            }
        }
        
    } catch (SQLException e) {
        System.out.println("SQL Error: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
    }
}
    
private void updateProfile() {
    String fullName = txtFullName.getText().trim();
    String email = txtEmail.getText().trim();
    String phone = txtPhone.getText().trim();
    String gender = (String) cmbGender.getSelectedItem();

    // Validation
    if (fullName.isEmpty() || email.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Full Name and Email are required!");
        return;
    }
    
    // Simple email validation
    if (!email.contains("@") || !email.contains(".")) {
        JOptionPane.showMessageDialog(this, "Please enter a valid email address!");
        return;
    }
    
    try (Connection con = DBConnection.connectDB();
         PreparedStatement pst = con.prepareStatement(
             "UPDATE users SET full_name = ?, email = ?, phone = ?, gender = ? WHERE username = ?")) {
        
        // Correct parameter indexes: 1-5 (not 1-11 like you had)
        pst.setString(1, fullName);
        pst.setString(2, email);
        pst.setString(3, phone);
        pst.setString(4, gender);
        pst.setString(5, username);
        
        int rowsUpdated = pst.executeUpdate();
        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            loadProfileDataSafe(); // Reload data
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile!");
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error updating profile: " + e.getMessage());
        e.printStackTrace();
    }
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtFullName = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        cmbGender = new javax.swing.JComboBox<>();
        btnUpdate = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        lblUserId = new javax.swing.JLabel();
        lblRole = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        User = new javax.swing.JLabel();
        FN = new javax.swing.JLabel();
        EM = new javax.swing.JLabel();
        Con = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(1000, 600));
        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("My Profile");
        add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, -1, -1));

        txtFullName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullNameActionPerformed(evt);
            }
        });
        add(txtFullName, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 70, 250, 30));
        add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 110, 250, 30));
        add(txtPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 150, 250, 30));

        jLabel11.setText("Gender:");
        add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 200, -1, -1));

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female", "Other" }));
        add(cmbGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 200, 250, 30));

        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpdate.setText("Update Profile");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 410, 190, 40));

        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 410, 170, 40));

        lblUserId.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblUserId.setText("User ID:");
        add(lblUserId, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 120, 130, 20));

        lblRole.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRole.setText("Position:");
        add(lblRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 150, 130, 20));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 70, 10, 250));

        jLabel6.setText("Full Name:");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 80, -1, -1));

        jLabel7.setText("Email:");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 120, -1, -1));

        jLabel8.setText("Phone:");
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 160, -1, -1));

        User.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        User.setText("Username:");
        add(User, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, 210, 20));

        FN.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        FN.setText("Full Name:");
        add(FN, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 220, 210, 20));

        EM.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EM.setText("Email:");
        add(EM, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 240, 20));

        Con.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        Con.setText("Contacts:");
        add(Con, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 220, 20));
    }// </editor-fold>//GEN-END:initComponents

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateProfile();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // Go back to appropriate dashboard based on role
        if (dashboard.getCurrentUser() != null) {
            // Reload dashboard (will determine admin/user based on session)
            if ("ADMIN".equals(getUserRole())) {
                dashboard.loginSuccess(username, "ADMIN");
            } else {
                dashboard.loginSuccess(username, "USER");
            }
        }
    }//GEN-LAST:event_btnBackActionPerformed

    private void txtFullNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFullNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFullNameActionPerformed

    private String getUserRole() {
        try (Connection con = DBConnection.connectDB();
             PreparedStatement pst = con.prepareStatement(
                 "SELECT role FROM users WHERE username = ?")) {
            
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "USER";
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Con;
    private javax.swing.JLabel EM;
    private javax.swing.JLabel FN;
    private javax.swing.JLabel User;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblUserId;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables
}