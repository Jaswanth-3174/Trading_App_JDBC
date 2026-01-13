package dbConnection;

import java.sql.*;

public class DatabaseConfig {
        static String url = "jdbc:mysql://localhost:3306/trading_app";
        static String username = "root_user1";
        static String password = "Root@123";
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
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void resetDatabase() throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        try {
            conn.setAutoCommit(false);
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            stmt.execute("TRUNCATE TABLE transactions");
            stmt.execute("TRUNCATE TABLE orders");
            stmt.execute("TRUNCATE TABLE stock_holdings");
            stmt.execute("TRUNCATE TABLE trading_accounts");
            stmt.execute("TRUNCATE TABLE users");
            stmt.execute("TRUNCATE TABLE demat_accounts");
            stmt.execute("TRUNCATE TABLE stocks");

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");

            stmt.execute("INSERT INTO stocks (stock_name) VALUES ('TCS'), ('SBI'), ('INFY'), ('NIFTY')");

            // promoter 1
            stmt.execute("insert into demat_accounts (pan_number, password) values('RAMS12R', 'Ab.11111')");
            stmt.execute("insert into users(username, password, demat_id, isPromoter) values('Ram', 'Ab.1111', 1, true);");
            stmt.execute("insert into trading_accounts(user_id, balance, reserved_balance) values (1, 8000.00, 0.00)");
            stmt.execute("insert into stock_holdings (demat_id, stock_id, total_quantity, reserved_quantity) values(1, 1, 1000, 300)");
            stmt.execute("insert into orders (user_id, stock_id, quantity, price, is_buy) values (1, 1, 300, 1500.00, false)");
            conn.commit();
            System.out.println("Database reset to initial state!");

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}