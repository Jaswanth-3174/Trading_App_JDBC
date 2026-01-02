package dao;

import dbConnection.DatabaseConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import trading.Stock;

public class StockDAO {
    public List<String> getStocks(){
        List<String> stocks = new ArrayList<>();
        String query = "SELECT * FROM stocks order by stock_id";
        try(Connection con = DatabaseConfig.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
        ){
            while (rs.next()){
                stocks.add(rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static int getStockIdByName(String stockName) throws SQLException {
        String query = "SELECT stock_id FROM stocks WHERE stock_name = ?";
        try(Connection con = DatabaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
        ){
            ps.setString(1, stockName);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        }
        return -1;
    }

    public static String getStockNameById(int stockId) throws SQLException{
        String query = "SELECT stock_name FROM stocks WHERE stock_id = ?";
        try(Connection con = DatabaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
        ){
            ps.setInt(1, stockId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }
        }
        return null;
    }
}
