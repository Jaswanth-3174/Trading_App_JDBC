// filepath: src/dao/StockDAO.java
package dao;

import config.DatabaseConfig;
import model.Stock;

import java.sql.*;
import java.util.*;

public class StockDAO {
    
    public List<Stock> findAll() throws SQLException {
        List<Stock> stocks = new ArrayList<>();
        String sql = "SELECT * FROM stocks ORDER BY stock_id";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Stock stock = new Stock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setStockName(rs.getString("stock_name"));
                stocks.add(stock);
            }
        }
        return stocks;
    }
    
    public Stock findById(int stockId) throws SQLException {
        String sql = "SELECT * FROM stocks WHERE stock_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, stockId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Stock stock = new Stock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setStockName(rs.getString("stock_name"));
                return stock;
            }
        }
        return null;
    }
    
    public Stock findByName(String stockName) throws SQLException {
        String sql = "SELECT * FROM stocks WHERE stock_name = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, stockName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Stock stock = new Stock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setStockName(rs.getString("stock_name"));
                return stock;
            }
        }
        return null;
    }
    
    public List<String> getAllStockNames() throws SQLException {
        List<String> names = new ArrayList<>();
        String sql = "SELECT stock_name FROM stocks ORDER BY stock_id";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                names.add(rs.getString("stock_name"));
            }
        }
        return names;
    }
}