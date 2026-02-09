package swing;

import main.Main;
import database.DBConnection;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminDashboard extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    
    public AdminDashboard(Main parent, String username) {
        this.dashboard = parent;
        this.username = username;
        initComponents();
        loadAllData();
    }
    
    private void loadAllData() {
        loadUsers();
        loadCustomers();
        loadOrders();
        loadShipping();
    }
    
    private void loadUsers() {
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT user_id, username, full_name, email, phone, role FROM users")) {
            usersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage());
        }
    }
    
    private void loadCustomers() {
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            customersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
    private void loadOrders() {
        String query = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount, " +
                      "o.payment_method, c.first_name, c.last_name, c.email " +
                      "FROM sales_orders o JOIN customers c ON o.customer_id = c.customer_id";
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ordersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }
    
    private void loadShipping() {
        String query = "SELECT s.shipment_id, s.carrier_name, s.shipping_weight, " +
                      "s.estimated_delivery_date, s.actual_delivery_date, s.shipment_status, " +
                      "o.order_id, c.first_name, c.last_name " +
                      "FROM shipping s " +
                      "JOIN sales_orders o ON s.order_id = o.order_id " +
                      "JOIN customers c ON o.customer_id = c.customer_id";
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            shippingTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading shipping: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        usersTable = new javax.swing.JTable();
        btnAddUser = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        customersTable = new javax.swing.JTable();
        btnAddCustomer = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        shippingTable = new javax.swing.JTable();
        btnAddShipping = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ordersTable = new javax.swing.JTable();
        btnAddOrder = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnProfile = new javax.swing.JButton();
        lblWelcome = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setPreferredSize(new java.awt.Dimension(1000, 600));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "User ID", "Username", "Full Name", "Email", "Phone", "Role"
            }
        ));
        jScrollPane1.setViewportView(usersTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnAddUser.setText("Add New User");
        btnAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddUserActionPerformed(evt);
            }
        });
        jPanel1.add(btnAddUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("User", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        customersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Customer ID", "First Name", "Last Name", "Email", "Phone", "Loyalty Points", "Address"
            }
        ));
        jScrollPane2.setViewportView(customersTable);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnAddCustomer.setText("Add Customer");
        btnAddCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCustomerActionPerformed(evt);
            }
        });
        jPanel2.add(btnAddCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Customer", jPanel2);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        shippingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Shipment ID", "Carrier", "Weight", "Est. Delivery", "Actual Delivery", "Status", "Order ID", "Customer"
            }
        ));
        jScrollPane4.setViewportView(shippingTable);

        jPanel4.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnAddShipping.setText("Add Shipping");
        btnAddShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddShippingActionPerformed(evt);
            }
        });
        jPanel4.add(btnAddShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Shipment", jPanel4);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ordersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Order ID", "Order Date", "Status", "Total Amount", "Payment Method", "Customer Name", "Email"
            }
        ));
        jScrollPane3.setViewportView(ordersTable);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnAddOrder.setText("Add Order");
        btnAddOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOrderActionPerformed(evt);
            }
        });
        jPanel3.add(btnAddOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Order Details", jPanel3);

        add(tabbedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 950, 420));

        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout.setText("LOGOUT");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 40, 100, 40));

        btnProfile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnProfile.setText("PROFILE");
        btnProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProfileActionPerformed(evt);
            }
        });
        add(btnProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 40, 100, 40));

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setText("Welcome, " + username);
        add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 2, 300, 40));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        // Add user dialog
        JOptionPane.showMessageDialog(this, "Add User functionality to be implemented");
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCustomerActionPerformed
        // Add customer dialog
        JOptionPane.showMessageDialog(this, "Add Customer functionality to be implemented");
    }//GEN-LAST:event_btnAddCustomerActionPerformed

    private void btnAddOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddOrderActionPerformed
        // Add order dialog
        JOptionPane.showMessageDialog(this, "Add Order functionality to be implemented");
    }//GEN-LAST:event_btnAddOrderActionPerformed

    private void btnAddShippingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddShippingActionPerformed
        // Add shipping dialog
        JOptionPane.showMessageDialog(this, "Add Shipping functionality to be implemented");
    }//GEN-LAST:event_btnAddShippingActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dashboard.logout();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProfileActionPerformed
        dashboard.showProfile();
    }//GEN-LAST:event_btnProfileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnAddShipping;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnProfile;
    private javax.swing.JTable customersTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JTable ordersTable;
    private javax.swing.JTable shippingTable;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable usersTable;
    // End of variables declaration//GEN-END:variables
}