package dao;

import config.DatabaseConfig;
import model.Order;

import java.sql.*;
import java.util.*;

public class OrderDAO {
    
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }
    
    public Order create(int userId, int stockId, int quantity, double price, boolean isBuy) throws SQLException {
        String sql = "INSERT INTO orders (user_id, stock_id, original_quantity, quantity, price, is_buy, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'OPEN')";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, stockId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, price);
            stmt.setBoolean(6, isBuy);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }
    
    /**
     * Get active BUY orders sorted by price DESC (highest first), then order_id ASC
     */
    public List<Order> getActiveBuyOrders(int stockId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.stock_id = ? AND o.is_buy = TRUE AND o.status IN ('OPEN', 'PARTIAL') " +
                     "ORDER BY o.price DESC, o.order_id ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
    
    /**
     * Get active SELL orders sorted by price ASC (lowest first), then order_id ASC
     */
    public List<Order> getActiveSellOrders(int stockId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.stock_id = ? AND o.is_buy = FALSE AND o.status IN ('OPEN', 'PARTIAL') " +
                     "ORDER BY o.price ASC, o.order_id ASC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
    
    public List<Order> getOrdersByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
    
    public List<Order> getActiveOrdersByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.user_id = ? AND o.status IN ('OPEN', 'PARTIAL') " +
                     "ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
    
    public boolean updateQuantityAndStatus(int orderId, int newQuantity, String newStatus) throws SQLException {
        String sql = "UPDATE orders SET quantity = ?, status = ? WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setString(2, newStatus);
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean modifyOrder(int orderId, int newOriginalQty, int newQty, double newPrice) throws SQLException {
        String sql = "UPDATE orders SET original_quantity = ?, quantity = ?, price = ?, " +
                     "status = CASE WHEN ? = ? THEN 'OPEN' ELSE 'PARTIAL' END " +
                     "WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newOriginalQty);
            stmt.setInt(2, newQty);
            stmt.setDouble(3, newPrice);
            stmt.setInt(4, newOriginalQty);
            stmt.setInt(5, newQty);
            stmt.setInt(6, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean cancelOrder(int orderId) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ? AND status IN ('OPEN', 'PARTIAL')";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Order> getAllActiveOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, s.stock_name, u.username FROM orders o " +
                     "JOIN stocks s ON o.stock_id = s.stock_id " +
                     "JOIN users u ON o.user_id = u.user_id " +
                     "WHERE o.status IN ('OPEN', 'PARTIAL') " +
                     "ORDER BY o.stock_id, o.is_buy DESC, o.price DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
        }
        return orders;
    }
    
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setStockId(rs.getInt("stock_id"));
        o.setStockName(rs.getString("stock_name"));
        o.setUsername(rs.getString("username"));
        o.setOriginalQuantity(rs.getInt("original_quantity"));
        o.setQuantity(rs.getInt("quantity"));
        o.setPrice(rs.getDouble("price"));
        o.setBuy(rs.getBoolean("is_buy"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        return o;
    }
}