package dao;

import config.DatabaseConfig;
import model.User;

import java.sql.*;
import java.util.*;

public class UserDAO {
    
    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
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
    
    public User findByDematId(int dematId) throws SQLException {
        String sql = "SELECT * FROM users WHERE demat_id = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dematId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }
    
    public boolean hasActiveUserWithDemat(int dematId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE demat_id = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dematId);
            return stmt.executeQuery().next();
        }
    }
    
    public User create(String username, String password, int dematId, boolean isPromoter) throws SQLException {
        String sql = "INSERT INTO users (username, password, demat_id, is_promoter) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, dematId);
            stmt.setBoolean(4, isPromoter);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }
    
    public boolean authenticate(int userId, String password) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND password = ? AND is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, password);
            return stmt.executeQuery().next();
        }
    }
    
    public boolean deactivate(int userId) throws SQLException {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_active = TRUE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }
    
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setDematId(rs.getInt("demat_id"));
        user.setPromoter(rs.getBoolean("is_promoter"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}