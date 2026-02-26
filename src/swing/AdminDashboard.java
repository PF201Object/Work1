package swing;

import classes.CustomerDialog;
import classes.OrderDialog;
import classes.ShippingDialog;
import classes.UserDialog;
import main.Main;
import database.DBConnection;
import database.DatabaseUtils;
import net.proteanit.sql.DbUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class AdminDashboard extends javax.swing.JPanel {
    private final Main dashboard;
    private final String username;
    private TableRowSorter<TableModel> usersSorter, customersSorter, ordersSorter, shippingSorter;

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
             ResultSet rs = stmt.executeQuery("SELECT customer_id, first_name, last_name, email, phone_number, item_purchased, default_shipping_address FROM customers")) {
            customersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage());
        }
    }
    
  private void loadOrders() {
    String query = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount, " +
                  "o.payment_method, c.first_name, c.last_name, c.email " +
                  "FROM sales_orders o " +
                  "JOIN customers c ON o.customer_id = c.customer_id";
    
    // Try to include created_by if the column exists
    try (Connection con = DBConnection.connectDB()) {
        // Check if created_by column exists
        boolean hasCreatedBy = false;
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(sales_orders)")) {
            while (rs.next()) {
                if ("created_by".equals(rs.getString("name"))) {
                    hasCreatedBy = true;
                    break;
                }
            }
        }
        
        // Use appropriate query based on column existence
        if (hasCreatedBy) {
            query = "SELECT o.order_id, o.order_date, o.order_status, o.total_amount, " +
                   "o.payment_method, c.first_name, c.last_name, c.email, o.created_by " +
                   "FROM sales_orders o " +
                   "JOIN customers c ON o.customer_id = c.customer_id";
        }
        
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            ordersTable.setModel(DbUtils.resultSetToTableModel(rs));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
    }
}

private void authorizeSelectedOrder() {
    int row = ordersTable.getSelectedRow();
    if (row >= 0) {
        int modelRow = ordersTable.convertRowIndexToModel(row);
        String orderId = ordersTable.getModel().getValueAt(modelRow, 0).toString();
        String currentStatus = ordersTable.getModel().getValueAt(modelRow, 2).toString();
        
        // Safely get created_by if column exists
        String createdBy = "";
        try {
            if (ordersTable.getColumnCount() > 8) {
                createdBy = ordersTable.getModel().getValueAt(modelRow, 8) != null ? 
                           ordersTable.getModel().getValueAt(modelRow, 8).toString() : "";
            }
        } catch (Exception e) {
            createdBy = "";
        }
        
        // Check if order is from a regular user (not admin)
        boolean isUserOrder = createdBy != null && !createdBy.contains("ADMIN") && !createdBy.isEmpty();
        
        if (currentStatus.equalsIgnoreCase("Pending")) {
            String message;
            if (isUserOrder) {
                message = "This order was created by a regular user. Authorize this order to mark as PAID?";
            } else {
                message = "Authorize this order to mark as PAID?";
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                message, 
                "Authorize Order", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // Update order status to "Paid"
                if (DatabaseUtils.executeUpdate("UPDATE sales_orders SET order_status = 'Paid' WHERE order_id = ?", orderId)) {
                    JOptionPane.showMessageDialog(this, "Order authorized successfully!");
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Error authorizing order");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Only pending orders can be authorized");
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select an order to authorize");
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
    
    private void filterTable(JTable table, String searchText) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        
        if (searchText.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        }
    }

private void updateSelectedUser() {
    int row = usersTable.getSelectedRow();
    if (row >= 0) {
        int modelRow = usersTable.convertRowIndexToModel(row);
        // Get the user_id as String instead of parsing to int
        String userId = usersTable.getModel().getValueAt(modelRow, 0).toString();
        UserDialog dialog = new UserDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Update User", userId);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUsers();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a user to update");
    }
}

private void deleteSelectedUser() {
    int row = usersTable.getSelectedRow();
    if (row >= 0) {
        int modelRow = usersTable.convertRowIndexToModel(row);
        String userId = usersTable.getModel().getValueAt(modelRow, 0).toString();
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this user?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (DatabaseUtils.executeUpdate("DELETE FROM users WHERE user_id = ?", userId)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Error deleting user");
            }
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please select a user to delete");
    }
}

    private void updateSelectedCustomer() {
      int row = customersTable.getSelectedRow();
      if (row >= 0) {
          int modelRow = customersTable.convertRowIndexToModel(row);
          // Get as String directly from the table, don't parse to int
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
            int customerId = Integer.parseInt(customersTable.getModel().getValueAt(modelRow, 0).toString());
            
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
        // Get as String directly from the table, don't parse to int
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
            int orderId = Integer.parseInt(ordersTable.getModel().getValueAt(modelRow, 0).toString());
            
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
        // Get the ID as String instead of parsing to int
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
        btnAddUser = new javax.swing.JButton();
        btnUpdateUser = new javax.swing.JButton();
        btnDeleteUser = new javax.swing.JButton();
        lblSearchUser = new javax.swing.JLabel();
        searchUser = new javax.swing.JTextField();
        btnSearchUser = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        usersTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        btnAddCustomer = new javax.swing.JButton();
        btnUpdateCustomer = new javax.swing.JButton();
        btnDeleteCustomer = new javax.swing.JButton();
        lblSearchCustomer = new javax.swing.JLabel();
        searchCustomers = new javax.swing.JTextField();
        btnSearchCustomers = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        customersTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        btnAuthorizeOrder = new javax.swing.JButton();
        btnAddOrder = new javax.swing.JButton();
        btnUpdateOrder = new javax.swing.JButton();
        btnDeleteOrder = new javax.swing.JButton();
        lblSearchOrder = new javax.swing.JLabel();
        searchOrders = new javax.swing.JTextField();
        btnSearchOrders = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        ordersTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        btnAddShipping = new javax.swing.JButton();
        btnUpdateShipping = new javax.swing.JButton();
        btnDeleteShipping = new javax.swing.JButton();
        lblSearchShipping = new javax.swing.JLabel();
        searchShipping = new javax.swing.JTextField();
        btnSearchShipping = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
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

        btnAddUser.setText("Add User");
        btnAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddUserActionPerformed(evt);
            }
        });
        jPanel1.add(btnAddUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateUser.setText("Update User");
        btnUpdateUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateUserActionPerformed(evt);
            }
        });
        jPanel1.add(btnUpdateUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 130, 30));

        btnDeleteUser.setText("Delete User");
        btnDeleteUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteUserActionPerformed(evt);
            }
        });
        jPanel1.add(btnDeleteUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, 30));

        lblSearchUser.setText("Search:");
        jPanel1.add(lblSearchUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 35, 50, 20));
        jPanel1.add(searchUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 200, 30));

        btnSearchUser.setText("Search");
        btnSearchUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchUserActionPerformed(evt);
            }
        });
        jPanel1.add(btnSearchUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, 30));

        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(usersTable);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("User", jPanel1);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddCustomer.setText("Add Customer");
        btnAddCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddCustomerActionPerformed(evt);
            }
        });
        jPanel2.add(btnAddCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateCustomer.setText("Update Customer");
        btnUpdateCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateCustomerActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdateCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 130, 30));

        btnDeleteCustomer.setText("Delete Customer");
        btnDeleteCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCustomerActionPerformed(evt);
            }
        });
        jPanel2.add(btnDeleteCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, 30));

        lblSearchCustomer.setText("Search:");
        jPanel2.add(lblSearchCustomer, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 35, 50, 20));
        jPanel2.add(searchCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 200, 30));

        btnSearchCustomers.setText("Search");
        btnSearchCustomers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchCustomersActionPerformed(evt);
            }
        });
        jPanel2.add(btnSearchCustomers, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, 30));

        customersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(customersTable);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Customer", jPanel2);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAuthorizeOrder.setText("AUTHORIZED PAYMENT");
        btnAuthorizeOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAuthorizeOrderActionPerformed(evt);
            }
        });
        jPanel3.add(btnAuthorizeOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 200, 30));

        btnAddOrder.setText("Add Order");
        btnAddOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddOrderActionPerformed(evt);
            }
        });
        jPanel3.add(btnAddOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateOrder.setText("Update Order");
        btnUpdateOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateOrderActionPerformed(evt);
            }
        });
        jPanel3.add(btnUpdateOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 120, 30));

        btnDeleteOrder.setText("Delete Order");
        btnDeleteOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteOrderActionPerformed(evt);
            }
        });
        jPanel3.add(btnDeleteOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(280, 30, 120, 30));

        lblSearchOrder.setText("Search:");
        jPanel3.add(lblSearchOrder, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 35, 50, 20));
        jPanel3.add(searchOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 30, 200, 30));

        btnSearchOrders.setText("Search");
        btnSearchOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchOrdersActionPerformed(evt);
            }
        });
        jPanel3.add(btnSearchOrders, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 30, 90, 30));

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
        jScrollPane3.setViewportView(ordersTable);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Order Details", jPanel3);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnAddShipping.setText("Add Shipping");
        btnAddShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddShippingActionPerformed(evt);
            }
        });
        jPanel4.add(btnAddShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 30, 120, 30));

        btnUpdateShipping.setText("Update Shipping");
        btnUpdateShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateShippingActionPerformed(evt);
            }
        });
        jPanel4.add(btnUpdateShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 30, 130, 30));

        btnDeleteShipping.setText("Delete Shipping");
        btnDeleteShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteShippingActionPerformed(evt);
            }
        });
        jPanel4.add(btnDeleteShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 30, 130, 30));

        lblSearchShipping.setText("Search:");
        jPanel4.add(lblSearchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 35, 50, 20));
        jPanel4.add(searchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(490, 30, 200, 30));

        btnSearchShipping.setText("Search");
        btnSearchShipping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchShippingActionPerformed(evt);
            }
        });
        jPanel4.add(btnSearchShipping, new org.netbeans.lib.awtextra.AbsoluteConstraints(700, 30, 90, 30));

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
        jScrollPane4.setViewportView(shippingTable);

        jPanel4.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 910, 290));

        tabbedPane.addTab("Shipment", jPanel4);

        add(tabbedPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, 950, 450));
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        UserDialog dialog = new UserDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New User", null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadUsers();
        }
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void btnUpdateUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateUserActionPerformed
               updateSelectedUser();
    }//GEN-LAST:event_btnUpdateUserActionPerformed

    private void btnDeleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteUserActionPerformed
        deleteSelectedUser();
    }//GEN-LAST:event_btnDeleteUserActionPerformed

    private void btnSearchUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchUserActionPerformed
        filterTable(usersTable, searchUser.getText());
    }//GEN-LAST:event_btnSearchUserActionPerformed

    private void btnAuthorizeOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAuthorizeOrderActionPerformed
    authorizeSelectedOrder();
    }//GEN-LAST:event_btnAuthorizeOrderActionPerformed

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
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnAuthorizeOrder;
    private javax.swing.JButton btnDeleteCustomer;
    private javax.swing.JButton btnDeleteOrder;
    private javax.swing.JButton btnDeleteShipping;
    private javax.swing.JButton btnDeleteUser;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnProfile;
    private javax.swing.JButton btnSearchCustomers;
    private javax.swing.JButton btnSearchOrders;
    private javax.swing.JButton btnSearchShipping;
    private javax.swing.JButton btnSearchUser;
    private javax.swing.JButton btnUpdateCustomer;
    private javax.swing.JButton btnUpdateOrder;
    private javax.swing.JButton btnUpdateShipping;
    private javax.swing.JButton btnUpdateUser;
    private javax.swing.JTable customersTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblSearchCustomer;
    private javax.swing.JLabel lblSearchOrder;
    private javax.swing.JLabel lblSearchShipping;
    private javax.swing.JLabel lblSearchUser;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JTable ordersTable;
    private javax.swing.JTextField searchCustomers;
    private javax.swing.JTextField searchOrders;
    private javax.swing.JTextField searchShipping;
    private javax.swing.JTextField searchUser;
    private javax.swing.JTable shippingTable;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable usersTable;
    // End of variables declaration//GEN-END:variables
}