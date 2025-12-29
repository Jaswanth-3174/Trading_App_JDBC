package dao;

import config.DatabaseConfig;
import model.Transaction;

import java.sql.*;
import java.util.*;

public class TransactionDAO {
    
    public Transaction create(int buyerId, int sellerId, int stockId, int quantity, 
                              double price, double total) throws SQLException {
        String sql = "INSERT INTO transactions (buyer_id, seller_id, stock_id, quantity, price, total) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, buyerId);
            stmt.setInt(2, sellerId);
            stmt.setInt(3, stockId);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, price);
            stmt.setDouble(6, total);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }
    
    public Transaction findById(int transactionId) throws SQLException {
        String sql = "SELECT t.*, s.stock_name, b.username as buyer_name, se.username as seller_name " +
                     "FROM transactions t " +
                     "JOIN stocks s ON t.stock_id = s.stock_id " +
                     "JOIN users b ON t.buyer_id = b.user_id " +
                     "JOIN users se ON t.seller_id = se.user_id " +
                     "WHERE t.transaction_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }
    
    public List<Transaction> getByUser(int userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, s.stock_name, b.username as buyer_name, se.username as seller_name " +
                     "FROM transactions t " +
                     "JOIN stocks s ON t.stock_id = s.stock_id " +
                     "JOIN users b ON t.buyer_id = b.user_id " +
                     "JOIN users se ON t.seller_id = se.user_id " +
                     "WHERE t.buyer_id = ? OR t.seller_id = ? " +
                     "ORDER BY t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        }
        return transactions;
    }
    
    public List<Transaction> getAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, s.stock_name, b.username as buyer_name, se.username as seller_name " +
                     "FROM transactions t " +
                     "JOIN stocks s ON t.stock_id = s.stock_id " +
                     "JOIN users b ON t.buyer_id = b.user_id " +
                     "JOIN users se ON t.seller_id = se.user_id " +
                     "ORDER BY t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapRow(rs));
            }
        }
        return transactions;
    }
    
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setBuyerId(rs.getInt("buyer_id"));
        t.setSellerId(rs.getInt("seller_id"));
        t.setStockId(rs.getInt("stock_id"));
        t.setStockName(rs.getString("stock_name"));
        t.setBuyerName(rs.getString("buyer_name"));
        t.setSellerName(rs.getString("seller_name"));
        t.setQuantity(rs.getInt("quantity"));
        t.setPrice(rs.getDouble("price"));
        t.setTotal(rs.getDouble("total"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}