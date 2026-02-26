package classes;

import database.DatabaseUtils;
import java.awt.Window;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TablePopupMenu {
    
    public static void addPopupMenu(JTable table, String type, Runnable refreshCallback) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        JMenuItem updateItem = new JMenuItem("Update");
        JMenuItem deleteItem = new JMenuItem("Delete");
        
        updateItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                // Get the ID as String to handle alphanumeric IDs (like "ADMIN-1000")
                String id = table.getModel().getValueAt(modelRow, 0).toString();
                handleUpdate(table, type, id, refreshCallback);
            }
        });
        
        deleteItem.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                // Get the ID as String to handle alphanumeric IDs
                String id = table.getModel().getValueAt(modelRow, 0).toString();
                handleDelete(type, id, refreshCallback);
            }
        });
        
        popupMenu.add(updateItem);
        popupMenu.add(deleteItem);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                        popupMenu.show(table, e.getX(), e.getY());
                    }
                }
            }
        });
    }
    
    private static void handleUpdate(JTable table, String type, String id, Runnable refreshCallback) {
        Window parentWindow = SwingUtilities.getWindowAncestor(table);
        JFrame parentFrame = (parentWindow instanceof JFrame) ? (JFrame) parentWindow : null;
        
        if (parentFrame == null) {
            JOptionPane.showMessageDialog(null, "Error: Could not find parent window");
            return;
        }
        
        switch (type) {
            case "user":
                UserDialog userDialog = new UserDialog(parentFrame, "Update User", id);
                userDialog.setVisible(true);
                if (userDialog.isSaved()) {
                    refreshCallback.run();
                }
                break;
            case "customer":
                CustomerDialog custDialog = new CustomerDialog(parentFrame, "Update Customer", id);
                custDialog.setVisible(true);
                if (custDialog.isSaved()) {
                    refreshCallback.run();
                }
                break;
            case "order":
                OrderDialog orderDialog = new OrderDialog(parentFrame, "Update Order", id);
                orderDialog.setVisible(true);
                if (orderDialog.isSaved()) {
                    refreshCallback.run();
                }
                break;
            case "shipping":
                ShippingDialog shipDialog = new ShippingDialog(parentFrame, "Update Shipping", id);
                shipDialog.setVisible(true);
                if (shipDialog.isSaved()) {
                    refreshCallback.run();
                }
                break;
        }
    }
    
    private static void handleDelete(String type, String id, Runnable refreshCallback) {
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Are you sure you want to delete this " + type + "?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String tableName;
            String idColumn;
            
            switch (type) {
                case "user":
                    tableName = "users";
                    idColumn = "user_id";
                    break;
                case "customer":
                    tableName = "customers";
                    idColumn = "customer_id";
                    break;
                case "order":
                    tableName = "sales_orders";
                    idColumn = "order_id";
                    break;
                case "shipping":
                    tableName = "shipping";
                    idColumn = "shipment_id";
                    break;
                default:
                    return;
            }
            
            String query = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
            boolean success = database.DatabaseUtils.executeUpdate(query, id);
            
            if (success) {
                JOptionPane.showMessageDialog(null, type + " deleted successfully");
                refreshCallback.run();
            } else {
                JOptionPane.showMessageDialog(null, "Error deleting " + type);
            }
        }
    }
}