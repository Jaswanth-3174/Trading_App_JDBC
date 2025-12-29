package dao;

import config.DatabaseConfig;
import model.DematAccount;

import java.sql.*;

public class DematAccountDAO {
    
    public DematAccount findById(int dematId) throws SQLException {
        String sql = "SELECT * FROM demat_accounts WHERE demat_id = ?";
        
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
    
    public DematAccount findByPan(String panNumber) throws SQLException {
        String sql = "SELECT * FROM demat_accounts WHERE pan_number = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, panNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapRow(rs);
            }
        }
        return null;
    }
    
    public DematAccount create(String panNumber, String password) throws SQLException {
        String sql = "INSERT INTO demat_accounts (pan_number, password) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, panNumber);
            stmt.setString(2, password);
            stmt.executeUpdate();
            
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                return findById(id);
            }
        }
        return null;
    }
    
    public boolean authenticate(String panNumber, String password) throws SQLException {
        String sql = "SELECT 1 FROM demat_accounts WHERE pan_number = ? AND password = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, panNumber);
            stmt.setString(2, password);
            return stmt.executeQuery().next();
        }
    }
    
    private DematAccount mapRow(ResultSet rs) throws SQLException {
        DematAccount da = new DematAccount();
        da.setDematId(rs.getInt("demat_id"));
        da.setPanNumber(rs.getString("pan_number"));
        da.setPassword(rs.getString("password"));
        return da;
    }
}