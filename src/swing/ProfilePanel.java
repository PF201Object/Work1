package swing;

import main.Main;
import database.DBConnection;
import database.DatabaseUtils;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ProfilePanel extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    
    private String currentImageBase64;
    
    public ProfilePanel(Main parent, String username) {
        this.dashboard = parent;
        this.username = username;
        initComponents();
        loadProfileDataSafe();
    }
    
private void loadProfileDataSafe() {
    System.out.println("Loading profile for: " + username);
    
    try (Connection con = DBConnection.connectDB()) {
        String query = "SELECT user_id, username, full_name, email, phone, role, gender, profile_image, " +
                      "SUBSTR(created_at, 1, 10) as join_date " +
                      "FROM users WHERE username = ?";
        
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                System.out.println("User found!");
                
                // Format user_id as Staff-1001, etc.
                String userId = rs.getString("user_id");
                String displayId;
                
                // Handle user_id format
                if (userId == null || userId.isEmpty()) {
                    displayId = "Staff-1001";
                } else if (userId.matches("\\d+")) {
                    // If it's just numbers, format as Staff-####
                    int idNum = Integer.parseInt(userId);
                    displayId = String.format("Staff-%04d", idNum + 1000);
                } else {
                    // Use as is (like ADMIN-1000)
                    displayId = userId;
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
                
                // Load profile image
                String profileImage = rs.getString("profile_image");
                if (profileImage != null && !profileImage.isEmpty()) {
                    currentImageBase64 = profileImage;
                    displayProfileImage(profileImage);
                } else {
                    // Set default avatar
                    setDefaultProfileImage();
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
             "UPDATE users SET full_name = ?, email = ?, phone = ?, gender = ?, profile_image = ? WHERE username = ?")) {
        
        pst.setString(1, fullName);
        pst.setString(2, email);
        pst.setString(3, phone);
        pst.setString(4, gender);
        pst.setString(5, currentImageBase64);
        pst.setString(6, username);
        
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

private void changeProfileImage() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select Profile Image");
    
    // Filter for image files
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "Image Files (jpg, png, gif, jpeg)", "jpg", "png", "gif", "jpeg");
    fileChooser.setFileFilter(filter);
    
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        
        try {
            // Read and resize image
            BufferedImage originalImage = ImageIO.read(selectedFile);
            BufferedImage resizedImage = resizeImage(originalImage, 150, 150);
            
            // Convert to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = getFileExtension(selectedFile).toLowerCase();
            if (format.equals("jpg") || format.equals("jpeg")) {
                ImageIO.write(resizedImage, "jpg", baos);
            } else {
                ImageIO.write(resizedImage, "png", baos);
            }
            byte[] imageBytes = baos.toByteArray();
            currentImageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            
            // Display the image
            displayProfileImage(currentImageBase64);
            
            // Enable remove button
            btnRemoveImage.setEnabled(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

private void removeProfileImage() {
    currentImageBase64 = null;
    setDefaultProfileImage();
    btnRemoveImage.setEnabled(false);
}

private void displayProfileImage(String base64Image) {
    try {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        ImageIcon icon = new ImageIcon(image.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
        lblProfileImage.setIcon(icon);
    } catch (Exception e) {
        e.printStackTrace();
        setDefaultProfileImage();
    }
}

private void setDefaultProfileImage() {
    // Create a default avatar with initials
    String initials = getInitials();
    BufferedImage defaultImage = createDefaultAvatar(initials);
    ImageIcon icon = new ImageIcon(defaultImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
    lblProfileImage.setIcon(icon);
}

private BufferedImage createDefaultAvatar(String initials) {
    int size = 150;
    BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();
    
    // Set background color
    g2d.setColor(new Color(52, 152, 219)); // Nice blue color
    g2d.fillOval(0, 0, size, size);
    
    // Draw text
    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("Arial", Font.BOLD, 60));
    FontMetrics fm = g2d.getFontMetrics();
    int x = (size - fm.stringWidth(initials)) / 2;
    int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
    g2d.drawString(initials, x, y);
    
    g2d.dispose();
    return image;
}

private String getInitials() {
    String fullName = txtFullName.getText().trim();
    if (fullName.isEmpty()) return "U";
    
    String[] names = fullName.split(" ");
    if (names.length >= 2) {
        return (names[0].charAt(0) + "" + names[names.length - 1].charAt(0)).toUpperCase();
    } else {
        return fullName.substring(0, 1).toUpperCase();
    }
}

private String getFileExtension(File file) {
    String name = file.getName();
    int lastIndexOf = name.lastIndexOf(".");
    if (lastIndexOf == -1) {
        return ""; // empty extension
    }
    return name.substring(lastIndexOf + 1);
}

private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = outputImage.createGraphics();
    g2d.drawImage(resultingImage, 0, 0, null);
    g2d.dispose();
    return outputImage;
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        lblProfileImage = new javax.swing.JLabel();
        btnChangeImage = new javax.swing.JButton();
        btnRemoveImage = new javax.swing.JButton();
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

        lblProfileImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblProfileImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lblProfileImage.setPreferredSize(new java.awt.Dimension(150, 150));
        add(lblProfileImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 70, 150, 150));

        btnChangeImage.setText("Change Image");
        btnChangeImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChangeImageActionPerformed(evt);
            }
        });
        add(btnChangeImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 230, 150, 30));

        btnRemoveImage.setText("Remove Image");
        btnRemoveImage.setEnabled(false);
        btnRemoveImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveImageActionPerformed(evt);
            }
        });
        add(btnRemoveImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 270, 150, 30));

        txtFullName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFullNameActionPerformed(evt);
            }
        });
        add(txtFullName, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 70, 250, 30));
        add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 110, 250, 30));
        add(txtPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 150, 250, 30));

        jLabel11.setText("Gender:");
        add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 200, -1, -1));

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female", "Other" }));
        add(cmbGender, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 200, 250, 30));

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
        add(lblUserId, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 70, 130, 20));

        lblRole.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblRole.setText("Position:");
        add(lblRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 100, 130, 20));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 70, 10, 250));

        jLabel6.setText("Full Name:");
        add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 80, -1, -1));

        jLabel7.setText("Email:");
        add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 120, -1, -1));

        jLabel8.setText("Phone:");
        add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 160, -1, -1));

        User.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        User.setText("Username:");
        add(User, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 140, 210, 20));

        FN.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        FN.setText("Full Name:");
        add(FN, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 170, 210, 20));

        EM.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EM.setText("Email:");
        add(EM, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 200, 240, 20));

        Con.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        Con.setText("Contacts:");
        add(Con, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 230, 220, 20));
    }// </editor-fold>//GEN-END:initComponents

    private void btnChangeImageActionPerformed(java.awt.event.ActionEvent evt) {
        changeProfileImage();
    }

    private void btnRemoveImageActionPerformed(java.awt.event.ActionEvent evt) {
        removeProfileImage();
    }

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
    private javax.swing.JButton btnChangeImage;
    private javax.swing.JButton btnRemoveImage;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cmbGender;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblProfileImage;
    private javax.swing.JLabel lblRole;
    private javax.swing.JLabel lblUserId;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFullName;
    private javax.swing.JTextField txtPhone;
    // End of variables declaration//GEN-END:variables
}