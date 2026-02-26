package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

    public static boolean executeUpdate(String query, Object... params) {
        try (Connection con = DBConnection.connectDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
        public static void executeQueryWithCallback(String query, QueryCallback callback, Object... params) {
        try (Connection con = DBConnection.connectDB();
             PreparedStatement pstmt = con.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                callback.process(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Functional interface for query callback
    public interface QueryCallback {
        void process(ResultSet rs) throws SQLException;
    }
    
    public static ResultSet executeQuery(String query, Object... params) throws SQLException {
    Connection con = DBConnection.connectDB();
    PreparedStatement pstmt = con.prepareStatement(query);
    
    for (int i = 0; i < params.length; i++) {
        pstmt.setObject(i + 1, params[i]);
    }
    
    return pstmt.executeQuery();
    }
    
    public static List<String> getCustomerNames() {
        List<String> customers = new ArrayList<>();
        String query = "SELECT customer_id, first_name || ' ' || last_name as full_name FROM customers";
        
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                customers.add(rs.getInt("customer_id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    public static List<String> getOrderIds() {
        List<String> orders = new ArrayList<>();
        String query = "SELECT order_id FROM sales_orders";
        
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                orders.add(String.valueOf(rs.getInt("order_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }
    
}