package dao;

import config.DatabaseConfig;
import model.TradingAccount;

import java.sql.*;

public class TradingAccountDAO {
    
    public TradingAccount findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM trading_accounts WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }
    
    public TradingAccount create(int userId, double initialBalance) throws SQLException {
        String sql = "INSERT INTO trading_accounts (user_id, balance, reserved_balance) VALUES (?, ?, 0)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, userId);
            stmt.setDouble(2, initialBalance);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findByUserId(userId);
            }
        }
        return null;
    }
    
    public boolean reserveBalance(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET reserved_balance = reserved_balance + ? " +
                     "WHERE user_id = ? AND (balance - reserved_balance) >= ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean releaseReservedBalance(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET reserved_balance = reserved_balance - ? " +
                     "WHERE user_id = ? AND reserved_balance >= ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean debit(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET balance = balance - ?, reserved_balance = reserved_balance - ? " +
                     "WHERE user_id = ? AND reserved_balance >= ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setDouble(2, amount);
            stmt.setInt(3, userId);
            stmt.setDouble(4, amount);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean credit(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET balance = balance + ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean addBalance(int userId, double amount) throws SQLException {
        return credit(userId, amount);
    }
    
    private TradingAccount mapRow(ResultSet rs) throws SQLException {
        TradingAccount ta = new TradingAccount();
        ta.setTradingId(rs.getInt("trading_id"));
        ta.setUserId(rs.getInt("user_id"));
        ta.setBalance(rs.getDouble("balance"));
        ta.setReservedBalance(rs.getDouble("reserved_balance"));
        return ta;
    }
}