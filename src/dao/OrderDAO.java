package dao;

import dbConnection.DatabaseConfig;
import trading.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private StockDAO stockDAO = new StockDAO();

    public Order findById(int orderId) throws SQLException{
        String query = "select o.*, s.stock_name from orders o join stocks s on o.stock_id = s.stock_id where o.order_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
        ){
            ps.setInt(1, orderId);
            ResultSet rs =  ps.executeQuery();
            if(rs.next()){
                return printRow(rs);
            }
        }
        return null;
    }

    public Order createOrder(int userId, String stockName, int quantity, double price, boolean isBuy) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        if (stockId < 0) {
            throw new SQLException("Stock not found: " + stockName);
        }
        String sql = "INSERT INTO orders (user_id, stock_id, quantity, price, is_buy) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, stockId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, price);
            stmt.setBoolean(5, isBuy);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }

    public Order printRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setStockId(rs.getInt("stock_id"));
        order.setQuantity(rs.getInt("quantity"));
        order.setPrice(rs.getDouble("price"));
        order.setBuy(rs.getBoolean("is_buy"));
        return order;
    }

    // all buy orders
    public List<Order> getBuyOrders(String stockName) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT o.*, s.stock_name 
            FROM orders o 
            JOIN stocks s ON o.stock_id = s.stock_id 
            WHERE o.stock_id = ? 
              AND o.is_buy = TRUE
            ORDER BY o.price DESC, o.order_id ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(printRow(rs));
            }
        }
        return orders;
    }

    // all sell orders
    public List<Order> getSellOrders(String stockName) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT o.*, s.stock_name FROM orders o JOIN stocks s ON o.stock_id = s.stock_id WHERE o.stock_id = ? AND o.is_buy = FALSE ORDER BY o.price ASC, o.order_id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(printRow(rs));
            }
        }
        return orders;
    }

    // user specific orders
    public List<Order> findByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();

        String sql = """
            SELECT o.*, s.stock_name 
            FROM orders o 
            JOIN stocks s ON o.stock_id = s.stock_id 
            WHERE o.user_id = ?
            ORDER BY o.order_id
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(printRow(rs));
            }
        }
        return orders;
    }

    // updates only quantity
    public boolean updateQuantity(int orderId, int newQuantity) throws SQLException {
        String sql = "UPDATE orders SET quantity = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    // updates both quantity and price
    public boolean modifyOrder(int orderId, int newQuantity, double newPrice) throws SQLException {
        String sql = "UPDATE orders SET quantity = ?, price = ? WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setDouble(2, newPrice);
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
}