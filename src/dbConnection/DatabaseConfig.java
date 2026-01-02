package dbConnection;

import java.sql.*;

public class DatabaseConfig {
        static String url = "jdbc:mysql://localhost:3306/trading_app";
        static String username = "jaswanth";
        static String password = "root";
        static String query = "select * from users";
        static Connection con = null;

        public static Connection getConnection(){
            try{
                con = DriverManager.getConnection(url, username, password);
            }catch (Exception e){
                e.printStackTrace();
            }
            return con;
        }

    public static void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    public static void commit() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.commit();
            con.setAutoCommit(true);
        }
    }

    public static void rollback() {
        try {
            if (con != null && !con.isClosed()) {
                con.rollback();
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("✓ Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset database to initial state for testing
     */
    public static void resetDatabase() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();

        try {
            conn.setAutoCommit(false);

            // Disable FK checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Clear data
            stmt.execute("TRUNCATE TABLE transactions");
            stmt.execute("TRUNCATE TABLE orders");
            stmt.execute("TRUNCATE TABLE stock_holdings");
            stmt.execute("TRUNCATE TABLE trading_accounts");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE demat_accounts");
            stmt.execute("TRUNCATE TABLE stocks");

            // Enable FK checks
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            // Insert initial stocks
            stmt.execute("INSERT INTO stocks (stock_name) VALUES ('TCS'), ('SBI'), ('INFY'), ('NIFTY')");

            // Insert promoter demat
            stmt.execute("INSERT INTO demat_accounts (pan_number, password) VALUES ('RAMS12R', 'Ab.11111')");

            // Insert promoter user
            stmt.execute("INSERT INTO users (username, password, demat_id, is_promoter) VALUES ('Ram', 'Ab.11111', 1, TRUE)");

            // Insert promoter trading account
            stmt.execute("INSERT INTO trading_accounts (user_id, balance, reserved_balance) VALUES (1, 8000.00, 0.00)");

            // Insert promoter stock holdings
            stmt.execute("INSERT INTO stock_holdings (demat_id, stock_id, total_quantity, reserved_quantity) VALUES (1, 1, 1000, 300)");

            // Insert initial sell order
            stmt.execute("INSERT INTO orders (user_id, stock_id, original_quantity, quantity, price, is_buy, status) VALUES (1, 1, 300, 300, 1500.00, FALSE, 'OPEN')");

            conn.commit();
            System.out.println("✓ Database reset to initial state!");

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
