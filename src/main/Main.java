package main;

import database.DBConnection;
import java.awt.*;
import javax.swing.*;
import swing.AdminDashboard;
import swing.LoginForm;
import swing.ProfilePanel;
import swing.RegistrationForm;
import swing.UserDashboard;

public final class Main extends javax.swing.JFrame {
    private String currentUser;
    private String currentRole;
    
    public Main() {
        // Initialize database first
        DBConnection.initializeDB();
        
        // Setup the GUI
        initComponents();
        showLoginForm();
        
        // Center the window
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    public void showLoginForm() {
        LoginForm loginForm = new LoginForm(this);
        mainPanel.removeAll();
        mainPanel.add(loginForm, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        setTitle("Shoe Inventory System - Login");
    }
    
    public void showRegistrationForm() {
        RegistrationForm regForm = new RegistrationForm(this);
        mainPanel.removeAll();
        mainPanel.add(regForm, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        setTitle("Shoe Inventory System - Registration");
    }
    
    public void loginSuccess(String username, String role) {
        this.currentUser = username;
        this.currentRole = role;
        
        if ("ADMIN".equals(role)) {
            showAdminDashboard();
        } else {
            showUserDashboard();
        }
    }
    
    private void showAdminDashboard() {
        AdminDashboard adminPanel = new AdminDashboard(this, currentUser);
        mainPanel.removeAll();
        mainPanel.add(adminPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        setTitle("Shoe Inventory System - Admin Dashboard");
    }
    
    private void showUserDashboard() {
        UserDashboard userPanel = new UserDashboard(this, currentUser);
        mainPanel.removeAll();
        mainPanel.add(userPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        setTitle("Shoe Inventory System - User Dashboard");
    }
    
    public void showProfile() {
        ProfilePanel profilePanel = new ProfilePanel(this, currentUser);
        mainPanel.removeAll();
        mainPanel.add(profilePanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        setTitle("Shoe Inventory System - Profile");
    }
    
    public void logout() {
        currentUser = null;
        currentRole = null;
        showLoginForm();
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Main method - entry point of the application
     * @param args
     */
    public static void main(String args[]) {
        // Use SwingUtilities.invokeLater to ensure thread safety
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Shoe Inventory System");
        setPreferredSize(new java.awt.Dimension(1000, 500));
        setResizable(false);

        mainPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables
}