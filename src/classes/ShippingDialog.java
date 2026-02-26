package classes;

import database.DatabaseUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ShippingDialog extends JDialog {
    private JComboBox<String> cmbOrder;
    private JTextField txtCarrier, txtWeight, txtEstDelivery, txtActualDelivery;
    private JComboBox<String> cmbStatus;
    private JButton btnSave, btnCancel;
    private boolean saved = false;
    private String shipmentId;  // Change from Integer to String
    private List<String> orders;
    
    public ShippingDialog(Frame parent, String title, String shipmentId) {
    super(parent, title, true);
    this.shipmentId = shipmentId;
    orders = DatabaseUtils.getOrderIds();
    initComponents();
    if (shipmentId != null && !shipmentId.isEmpty()) {
        loadShippingData();
    }
    setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(400, 350);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Order ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Order ID:"), gbc);
        gbc.gridx = 1;
        cmbOrder = new JComboBox<>(orders.toArray(new String[0]));
        panel.add(cmbOrder, gbc);
        
        // Carrier
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Carrier:"), gbc);
        gbc.gridx = 1;
        txtCarrier = new JTextField(20);
        panel.add(txtCarrier, gbc);
        
        // Weight
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 1;
        txtWeight = new JTextField(20);
        panel.add(txtWeight, gbc);
        
        // Estimated Delivery
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Est. Delivery (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtEstDelivery = new JTextField(20);
        txtEstDelivery.setText(LocalDate.now().plusDays(7).toString());
        panel.add(txtEstDelivery, gbc);
        
        // Actual Delivery
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Actual Delivery:"), gbc);
        gbc.gridx = 1;
        txtActualDelivery = new JTextField(20);
        panel.add(txtActualDelivery, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"Processing", "Shipped", "In Transit", "Out for Delivery", "Delivered", "Returned"});
        panel.add(cmbStatus, gbc);
        
        add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        btnSave = new JButton("Save");
        btnCancel = new JButton("Cancel");
        
        btnSave.addActionListener(e -> saveShipping());
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadShippingData() {
        try {
            String query = "SELECT * FROM shipping WHERE shipment_id = ?";
            ResultSet rs = DatabaseUtils.executeQuery(query, shipmentId);
            if (rs.next()) {
                String orderId = String.valueOf(rs.getInt("order_id"));
                cmbOrder.setSelectedItem(orderId);
                txtCarrier.setText(rs.getString("carrier_name"));
                txtWeight.setText(rs.getString("shipping_weight"));
                
                Date estDate = rs.getDate("estimated_delivery_date");
                if (estDate != null) {
                    txtEstDelivery.setText(estDate.toString());
                }
                
                Date actDate = rs.getDate("actual_delivery_date");
                if (actDate != null) {
                    txtActualDelivery.setText(actDate.toString());
                }
                
                cmbStatus.setSelectedItem(rs.getString("shipment_status"));
            }
            rs.getStatement().getConnection().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveShipping() {
        if (cmbOrder.getSelectedIndex() == -1 || txtCarrier.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields");
            return;
        }
        
        int orderId = Integer.parseInt((String)cmbOrder.getSelectedItem());
        
        String query;
        if (shipmentId == null) {
            query = "INSERT INTO shipping (order_id, carrier_name, shipping_weight, estimated_delivery_date, actual_delivery_date, shipment_status) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            query = "UPDATE shipping SET order_id=?, carrier_name=?, shipping_weight=?, estimated_delivery_date=?, actual_delivery_date=?, shipment_status=? WHERE shipment_id=?";
        }
        
        try {
            boolean success;
            if (shipmentId == null) {
                success = DatabaseUtils.executeUpdate(query,
                    orderId,
                    txtCarrier.getText(),
                    txtWeight.getText().isEmpty() ? null : Double.parseDouble(txtWeight.getText()),
                    txtEstDelivery.getText().isEmpty() ? null : txtEstDelivery.getText(),
                    txtActualDelivery.getText().isEmpty() ? null : txtActualDelivery.getText(),
                    cmbStatus.getSelectedItem());
            } else {
                success = DatabaseUtils.executeUpdate(query,
                    orderId,
                    txtCarrier.getText(),
                    txtWeight.getText().isEmpty() ? null : Double.parseDouble(txtWeight.getText()),
                    txtEstDelivery.getText().isEmpty() ? null : txtEstDelivery.getText(),
                    txtActualDelivery.getText().isEmpty() ? null : txtActualDelivery.getText(),
                    cmbStatus.getSelectedItem(),
                    shipmentId);
            }
            
            if (success) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving shipping information");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Weight must be a number");
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}