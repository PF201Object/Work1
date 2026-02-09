package swing;

import main.Main;
import database.DBConnection;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.sql.*;

public class UserDashboard extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    
    public UserDashboard(Main parent, String username) {
        this.dashboard = parent;
        this.username = username;
        initComponents();
        loadAllData();
    }
    
    private void loadAllData() {
        loadCustomers();
        loadOrders();
        loadShipping();
    }
    
    private void loadCustomers() {
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM customers")) {
            custTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
    private void loadOrders() {
        String query = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount, " +
                      "o.payment_method, c.first_name, c.last_name " +
                      "FROM sales_orders o JOIN customers c ON o.customer_id = c.customer_id";
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            orderTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }
    
    private void loadShipping() {
        String query = "SELECT s.shipment_id, s.carrier_name, s.shipping_weight, " +
                      "s.estimated_delivery_date, s.shipment_status, o.order_id " +
                      "FROM shipping s JOIN sales_orders o ON s.order_id = o.order_id";
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            shipTable.setModel(DbUtils.resultSetToTableModel(rs));
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
        custTable = new javax.swing.JTable();
        btnCustAdd = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        orderTable = new javax.swing.JTable();
        btnOrderAdd = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        shipTable = new javax.swing.JTable();
        btnShipAdd = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnProfile = new javax.swing.JButton();
        lblWelcome = new javax.swing.JLabel();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        custTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Customer ID", "First Name", "Last Name", "Email", "Phone", "Loyalty Points", "Address"
            }
        ));
        custTable.setPreferredSize(new java.awt.Dimension(450, 0));
        jScrollPane1.setViewportView(custTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnCustAdd.setText("Add Customer");
        btnCustAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustAddActionPerformed(evt);
            }
        });
        jPanel1.add(btnCustAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Customer", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        orderTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Order ID", "Order Date", "Status", "Total Amount", "Payment Method", "Customer"
            }
        ));
        jScrollPane2.setViewportView(orderTable);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnOrderAdd.setText("Add Order");
        btnOrderAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOrderAddActionPerformed(evt);
            }
        });
        jPanel2.add(btnOrderAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Order Details", jPanel2);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        shipTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Shipment ID", "Carrier", "Weight", "Est. Delivery", "Status", "Order ID"
            }
        ));
        jScrollPane3.setViewportView(shipTable);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 910, 290));

        btnShipAdd.setText("Add Shipping");
        btnShipAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShipAddActionPerformed(evt);
            }
        });
        jPanel3.add(btnShipAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        tabbedPane.addTab("Shipment", jPanel3);

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
        add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 0, 300, 40));
    }// </editor-fold>//GEN-END:initComponents

    private void btnCustAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustAddActionPerformed
        JOptionPane.showMessageDialog(this, "Add Customer functionality to be implemented");
    }//GEN-LAST:event_btnCustAddActionPerformed

    private void btnOrderAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrderAddActionPerformed
        JOptionPane.showMessageDialog(this, "Add Order functionality to be implemented");
    }//GEN-LAST:event_btnOrderAddActionPerformed

    private void btnShipAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShipAddActionPerformed
        JOptionPane.showMessageDialog(this, "Add Shipping functionality to be implemented");
    }//GEN-LAST:event_btnShipAddActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        dashboard.logout();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProfileActionPerformed
        dashboard.showProfile();
    }//GEN-LAST:event_btnProfileActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustAdd;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnOrderAdd;
    private javax.swing.JButton btnProfile;
    private javax.swing.JButton btnShipAdd;
    private javax.swing.JTable custTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JTable orderTable;
    private javax.swing.JTable shipTable;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}