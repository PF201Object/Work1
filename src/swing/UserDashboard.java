package swing;

import classes.CustomerDialog;
import classes.OrderDialog;
import classes.ShippingDialog;
import main.Main;
import database.DBConnection;
import database.DatabaseUtils;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class UserDashboard extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    
    // Search and action button
    private TableRowSorter<TableModel> customersSorter, ordersSorter, shippingSorter;
    
    public UserDashboard(Main parent, String username) {
        this.dashboard = parent;
        this.username = username;
        initComponents();
        loadAllData();
            debugTableVisibility(); // Add this line
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
        
        // Count rows to verify data exists
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
        }
        System.out.println("Number of customers found: " + rowCount);
        
        // Can't reuse rs here - it's already used in the try-with-resources
        // Instead, create a new ResultSet
        Statement newStmt = con.createStatement();
        ResultSet newRs = newStmt.executeQuery("SELECT * FROM customers");
        
        // Set the table model
        customersTable.setModel(DbUtils.resultSetToTableModel(newRs));
        
        // Close the new resources
        newRs.close();
        newStmt.close();
        
        // Force table to refresh
        customersTable.revalidate();
        customersTable.repaint();
        
        System.out.println("Table model set with " + customersTable.getRowCount() + " rows");
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        e.printStackTrace();
    }
}
    private void debugTableVisibility() {
    System.out.println("=== Debug Table Visibility ===");
    System.out.println("customersTable exists: " + (customersTable != null));
    System.out.println("jScrollPane1 exists: " + (jScrollPane1 != null));
    System.out.println("jPanel1 exists: " + (jPanel1 != null));
    System.out.println("tabbedPane exists: " + (tabbedPane != null));
    
    System.out.println("customersTable width: " + customersTable.getWidth());
    System.out.println("customersTable height: " + customersTable.getHeight());
    System.out.println("customersTable visible: " + customersTable.isVisible());
    
    System.out.println("jScrollPane1 visible: " + jScrollPane1.isVisible());
    System.out.println("jPanel1 visible: " + jPanel1.isVisible());
    System.out.println("tabbedPane visible: " + tabbedPane.isVisible());
    
    System.out.println("customersTable row count: " + customersTable.getRowCount());
    System.out.println("customersTable column count: " + customersTable.getColumnCount());
    System.out.println("==============================");
}
    
    private void loadOrders() {
        String query = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount, " +
                      "o.payment_method, c.first_name, c.last_name " +
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
                      "s.estimated_delivery_date, s.shipment_status, o.order_id " +
                      "FROM shipping s JOIN sales_orders o ON s.order_id = o.order_id";
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            shippingTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading shipping: " + e.getMessage());
        }
    }
    
    private void filterTable(JTable table, String searchText) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        
        if (searchText.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }
    
    private void updateSelectedCustomer() {
        int row = customersTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = customersTable.convertRowIndexToModel(row);
            String customerId = customersTable.getModel().getValueAt(modelRow, 0).toString();
            CustomerDialog dialog = new CustomerDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Update Customer", customerId);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadCustomers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to update");
        }
    }
    
    private void deleteSelectedCustomer() {
        int row = customersTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = customersTable.convertRowIndexToModel(row);
            String customerId = customersTable.getModel().getValueAt(modelRow, 0).toString();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this customer?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (DatabaseUtils.executeUpdate("DELETE FROM customers WHERE customer_id = ?", customerId)) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully");
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting customer");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete");
        }
    }
    
    private void updateSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = ordersTable.convertRowIndexToModel(row);
            String orderId = ordersTable.getModel().getValueAt(modelRow, 0).toString();
            OrderDialog dialog = new OrderDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Update Order", orderId);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadOrders();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to update");
        }
    }
    
    private void deleteSelectedOrder() {
        int row = ordersTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = ordersTable.convertRowIndexToModel(row);
            String orderId = ordersTable.getModel().getValueAt(modelRow, 0).toString();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this order?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (DatabaseUtils.executeUpdate("DELETE FROM sales_orders WHERE order_id = ?", orderId)) {
                    JOptionPane.showMessageDialog(this, "Order deleted successfully");
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting order");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to delete");
        }
    }
    
    private void updateSelectedShipping() {
        int row = shippingTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = shippingTable.convertRowIndexToModel(row);
            String shipmentId = shippingTable.getModel().getValueAt(modelRow, 0).toString();
            ShippingDialog dialog = new ShippingDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Update Shipping", shipmentId);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                loadShipping();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a shipment to update");
        }
    }
    
    private void deleteSelectedShipping() {
        int row = shippingTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = shippingTable.convertRowIndexToModel(row);
            String shipmentId = shippingTable.getModel().getValueAt(modelRow, 0).toString();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this shipment?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (DatabaseUtils.executeUpdate("DELETE FROM shipping WHERE shipment_id = ?", shipmentId)) {
                    JOptionPane.showMessageDialog(this, "Shipment deleted successfully");
                    loadShipping();
                } else {
                    JOptionPane.showMessageDialog(this, "Error deleting shipment");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a shipment to delete");
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWelcome = new javax.swing.JLabel();
        btnProfile = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        btnAddCustomer = new javax.swing.JButton();
        btnUpdateCustomer = new javax.swing.JButton();
        btnDeleteCustomer = new javax.swing.JButton();
        lblSearchCustomer = new javax.swing.JLabel();
        searchCustomers = new javax.swing.JTextField();
        btnSearchCustomers = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        customersTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnAddOrder = new javax.swing.JButton();
        btnUpdateOrder = new javax.swing.JButton();
        btnDeleteOrder = new javax.swing.JButton();
        lblSearchOrder = new javax.swing.JLabel();
        searchOrders = new javax.swing.JTextField();
        btnSearchOrders = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ordersTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnAddShipping = new javax.swing.JButton();
        btnUpdateShipping = new javax.swing.JButton();
        btnDeleteShipping = new javax.swing.JButton();
        lblSearchShipping = new javax.swing.JLabel();
        searchShipping = new javax.swing.JTextField();
        btnSearchShipping = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        shippingTable = new javax.swing.JTable();

        setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblWelcome.setText("Welcome, " + username);
        add(lblWelcome, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, 400, 40));

        btnProfile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnProfile.setText("PROFILE");
        btnProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProfileActionPerformed(evt);
            }
        });
        add(btnProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 20, 100, 40));

        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogout.setText("LOGOUT");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 20, 100, 40));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddCustomer.setText("Add Customer");
        btnAddCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCustomerActionPerformed(evt);
            }
        });
        jPanel1.add(btnAddCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateCustomer.setText("Update Customer");
        btnUpdateCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateCustomerActionPerformed(evt);
            }
        });
        jPanel1.add(btnUpdateCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 130, 30));

        btnDeleteCustomer.setText("Delete Customer");
        btnDeleteCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCustomerActionPerformed(evt);
            }
        });
        jPanel1.add(btnDeleteCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, 30));

        lblSearchCustomer.setText("Search:");
        jPanel1.add(lblSearchCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 35, 50, 20));
        jPanel1.add(searchCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 200, 30));

        btnSearchCustomers.setText("Search");
        btnSearchCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchCustomersActionPerformed(evt);
            }
        });
        jPanel1.add(btnSearchCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, 30));

        customersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(customersTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Customer", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddOrder.setText("Add Order");
        btnAddOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOrderActionPerformed(evt);
            }
        });
        jPanel2.add(btnAddOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateOrder.setText("Update Order");
        btnUpdateOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateOrderActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdateOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 120, 30));

        btnDeleteOrder.setText("Delete Order");
        btnDeleteOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteOrderActionPerformed(evt);
            }
        });
        jPanel2.add(btnDeleteOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 30, 120, 30));

        lblSearchOrder.setText("Search:");
        jPanel2.add(lblSearchOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 35, 50, 20));
        jPanel2.add(searchOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, 200, 30));

        btnSearchOrders.setText("Search");
        btnSearchOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchOrdersActionPerformed(evt);
            }
        });
        jPanel2.add(btnSearchOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 30, 90, 30));

        ordersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Order ID", "Order Date", "Status", "Total Amount", "Payment Method", "Customer"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(ordersTable);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Order Details", jPanel2);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddShipping.setText("Add Shipping");
        btnAddShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddShippingActionPerformed(evt);
            }
        });
        jPanel3.add(btnAddShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateShipping.setText("Update Shipping");
        btnUpdateShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateShippingActionPerformed(evt);
            }
        });
        jPanel3.add(btnUpdateShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 130, 30));

        btnDeleteShipping.setText("Delete Shipping");
        btnDeleteShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteShippingActionPerformed(evt);
            }
        });
        jPanel3.add(btnDeleteShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, 30));

        lblSearchShipping.setText("Search:");
        jPanel3.add(lblSearchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 35, 50, 20));
        jPanel3.add(searchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 200, 30));

        btnSearchShipping.setText("Search");
        btnSearchShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchShippingActionPerformed(evt);
            }
        });
        jPanel3.add(btnSearchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, 30));

        shippingTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Shipment ID", "Carrier", "Weight", "Est. Delivery", "Status", "Order ID"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(shippingTable);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Shipment", jPanel3);

        add(tabbedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 950, 450));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        CustomerDialog dialog = new CustomerDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Customer", (String) null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadCustomers();
        }
    }

    private void btnUpdateCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        updateSelectedCustomer();
    }

    private void btnDeleteCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        deleteSelectedCustomer();
    }

    private void btnSearchCustomersActionPerformed(java.awt.event.ActionEvent evt) {
        filterTable(customersTable, searchCustomers.getText());
    }

    private void btnAddOrderActionPerformed(java.awt.event.ActionEvent evt) {
        OrderDialog dialog = new OrderDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Order", (String) null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadOrders();
        }
    }

    private void btnUpdateOrderActionPerformed(java.awt.event.ActionEvent evt) {
        updateSelectedOrder();
    }

    private void btnDeleteOrderActionPerformed(java.awt.event.ActionEvent evt) {
        deleteSelectedOrder();
    }

    private void btnSearchOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        filterTable(ordersTable, searchOrders.getText());
    }

    private void btnAddShippingActionPerformed(java.awt.event.ActionEvent evt) {
        ShippingDialog dialog = new ShippingDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Shipping", (String) null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadShipping();
        }
    }

    private void btnUpdateShippingActionPerformed(java.awt.event.ActionEvent evt) {
        updateSelectedShipping();
    }

    private void btnDeleteShippingActionPerformed(java.awt.event.ActionEvent evt) {
        deleteSelectedShipping();
    }

    private void btnSearchShippingActionPerformed(java.awt.event.ActionEvent evt) {
        filterTable(shippingTable, searchShipping.getText());
    }

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {
        dashboard.logout();
    }

    private void btnProfileActionPerformed(java.awt.event.ActionEvent evt) {
        dashboard.showProfile();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCustomer;
    private javax.swing.JButton btnAddOrder;
    private javax.swing.JButton btnAddShipping;
    private javax.swing.JButton btnDeleteCustomer;
    private javax.swing.JButton btnDeleteOrder;
    private javax.swing.JButton btnDeleteShipping;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnProfile;
    private javax.swing.JButton btnSearchCustomers;
    private javax.swing.JButton btnSearchOrders;
    private javax.swing.JButton btnSearchShipping;
    private javax.swing.JButton btnUpdateCustomer;
    private javax.swing.JButton btnUpdateOrder;
    private javax.swing.JButton btnUpdateShipping;
    private javax.swing.JTable customersTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblSearchCustomer;
    private javax.swing.JLabel lblSearchOrder;
    private javax.swing.JLabel lblSearchShipping;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JTable ordersTable;
    private javax.swing.JTextField searchCustomers;
    private javax.swing.JTextField searchOrders;
    private javax.swing.JTextField searchShipping;
    private javax.swing.JTable shippingTable;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}