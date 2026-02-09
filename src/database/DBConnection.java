package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnection {
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:shoe_inventory.db");
            System.out.println("SQLite Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }
    
        public static void initializeDB() {
        try (Connection con = DBConnection.connectDB();
             Statement stmt = con.createStatement()) {
            
            // Updated users table with INTEGER AUTOINCREMENT
            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +  // INTEGER AUTOINCREMENT
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "password VARCHAR(100) NOT NULL, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "phone VARCHAR(20), " +
                "profile_image TEXT, " +
                "gender VARCHAR(10), " +
                "role VARCHAR(20) DEFAULT 'USER', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_login TIMESTAMP)";
            
            String createCustomers = "CREATE TABLE IF NOT EXISTS customers (" +
                "customer_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name VARCHAR(50) NOT NULL, " +
                "last_name VARCHAR(50) NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "phone_number VARCHAR(20) NOT NULL, " +
                "loyalty_points INTEGER DEFAULT 0, " +
                "default_shipping_address TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            String createOrders = "CREATE TABLE IF NOT EXISTS sales_orders (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "order_status VARCHAR(20) DEFAULT 'Pending', " +
                "total_amount DECIMAL(10,2) NOT NULL, " +
                "payment_method VARCHAR(30), " +
                "tracking_number VARCHAR(50), " +
                "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))";
            
            String createShipping = "CREATE TABLE IF NOT EXISTS shipping (" +
                "shipment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "order_id INTEGER NOT NULL, " +
                "carrier_name VARCHAR(50), " +
                "shipping_weight DECIMAL(10,2), " +
                "estimated_delivery_date DATE, " +
                "actual_delivery_date DATE, " +
                "shipment_status VARCHAR(20) DEFAULT 'Processing', " +
                "FOREIGN KEY (order_id) REFERENCES sales_orders(order_id))";
            
            stmt.execute(createUsers);
            stmt.execute(createCustomers);
            stmt.execute(createOrders);
            stmt.execute(createShipping);

            try {
                // First check if admin exists
                Statement checkStmt = con.createStatement();
                ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'");
                if (rs.next() && rs.getInt(1) == 0) {
                    // Insert admin with a special user_id
                    stmt.execute("INSERT INTO users (user_id, username, password, full_name, email, phone, role) " +
                                "VALUES ('ADMIN-001', 'admin', 'admin123', 'System Administrator', 'admin@shoeinventory.com', '+1234567890', 'ADMIN')");
                }
            } catch (Exception e) {
                System.out.println("Admin user setup: " + e.getMessage());
            }
            
            System.out.println("Database initialized successfully!");
            
        } catch (Exception e) {
            System.out.println("Database initialization failed: " + e);
            e.printStackTrace();
        }
    }
}