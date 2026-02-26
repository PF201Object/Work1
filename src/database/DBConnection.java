package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // Add connection pool settings to avoid locking
            String url = "jdbc:sqlite:shoe_inventory.db";
            Properties props = new Properties();
            props.setProperty("journal_mode", "WAL"); // Write-Ahead Logging mode
            props.setProperty("synchronous", "NORMAL");
            con = DriverManager.getConnection(url, props);
            System.out.println("SQLite Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }
    
  public static void initializeDB() {
    try (Connection con = DBConnection.connectDB();
         Statement stmt = con.createStatement()) {
        
        // Enable WAL mode for better concurrency
        stmt.execute("PRAGMA journal_mode=WAL");
        stmt.execute("PRAGMA synchronous=NORMAL");
        
        // Your existing table creation code...
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
            "user_id TEXT PRIMARY KEY, " +
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
            "item_purchased VARCHAR(50), " +
            "loyalty_points INTEGER DEFAULT 0, " +
            "default_shipping_address TEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        
        String createOrders = "CREATE TABLE IF NOT EXISTS sales_orders (" +
            "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_id INTEGER NOT NULL, " +
            "created_by TEXT, " +  // Add this line
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
        
        // Run migration to ensure created_by column exists
        migrateDatabase();

        // Insert default admin if not exists
        try (Statement checkStmt = con.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users WHERE username = 'admin'")) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (user_id, username, password, full_name, email, phone, role) " +
                            "VALUES ('ADMIN-1000', 'admin', 'admin123', 'System Administrator', 'admin@shoeinventory.com', '+1234567890', 'ADMIN')");
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
    public static void migrateDatabase() {
    try (Connection con = DBConnection.connectDB();
         Statement stmt = con.createStatement()) {
        
        // Check if created_by column exists
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(sales_orders)");
        boolean hasCreatedBy = false;
        while (rs.next()) {
            if ("created_by".equals(rs.getString("name"))) {
                hasCreatedBy = true;
                break;
            }
        }
        
        // Add column if it doesn't exist
        if (!hasCreatedBy) {
            stmt.execute("ALTER TABLE sales_orders ADD COLUMN created_by TEXT");
            System.out.println("Added created_by column to sales_orders table");
        }
        
    } catch (SQLException e) {
        System.out.println("Migration error: " + e.getMessage());
    }
}
}