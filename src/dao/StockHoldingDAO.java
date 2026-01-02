package dao;

import dbConnection.DatabaseConfig;
import trading.StockHolding;

import java.sql.*;
import java.util.*;

public class StockHoldingDAO {

    public List<StockHolding> findByDematId(int dematId) throws SQLException {
        List<StockHolding> holdings = new ArrayList<>();
        String sql = "SELECT h.*, s.stock_name FROM stock_holdings h " +
                "JOIN stocks s ON h.stock_id = s.stock_id " +
                "WHERE h.demat_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dematId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                holdings.add(printRow(rs));
            }
        }
        return holdings;
    }

    public StockHolding findByDematAndStock(int dematId, int stockId) throws SQLException {
        String sql = "SELECT h.*, s.stock_name FROM stock_holdings h " +
                "JOIN stocks s ON h.stock_id = s.stock_id " +
                "WHERE h.demat_id = ? AND h.stock_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dematId);
            stmt.setInt(2, stockId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return printRow(rs);
            }
        }
        return null;
    }

    public boolean reserveStocks(int dematId, int stockId, int quantity) throws SQLException {
        String sql = "UPDATE stock_holdings SET reserved_quantity = reserved_quantity + ? " +
                "WHERE demat_id = ? AND stock_id = ? AND (total_quantity - reserved_quantity) >= ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, dematId);
            stmt.setInt(3, stockId);
            stmt.setInt(4, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean releaseReservedStocks(int dematId, int stockId, int quantity) throws SQLException {
        String sql = "UPDATE stock_holdings SET reserved_quantity = reserved_quantity - ? " +
                "WHERE demat_id = ? AND stock_id = ? AND reserved_quantity >= ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, dematId);
            stmt.setInt(3, stockId);
            stmt.setInt(4, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean sellShares(int dematId, int stockId, int quantity) throws SQLException {
        String sql = "UPDATE stock_holdings SET total_quantity = total_quantity - ?, " +
                "reserved_quantity = reserved_quantity - ? " +
                "WHERE demat_id = ? AND stock_id = ? AND reserved_quantity >= ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, dematId);
            stmt.setInt(4, stockId);
            stmt.setInt(5, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean addShares(int dematId, int stockId, int quantity) throws SQLException {
        // First try to update existing holding
        String updateSql = "UPDATE stock_holdings SET total_quantity = total_quantity + ? " +
                "WHERE demat_id = ? AND stock_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, dematId);
            stmt.setInt(3, stockId);

            if (stmt.executeUpdate() > 0) {
                return true;
            }
        }

        // If no existing holding, insert new one
        String insertSql = "INSERT INTO stock_holdings (demat_id, stock_id, total_quantity, reserved_quantity) " +
                "VALUES (?, ?, ?, 0)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {

            stmt.setInt(1, dematId);
            stmt.setInt(2, stockId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        }
    }

    private StockHolding printRow(ResultSet rs) throws SQLException {
        StockHolding h = new StockHolding();
        h.setStockHoldingId(rs.getInt("holding_id"));
        h.setDematId(rs.getInt("demat_id"));
        h.setStockId(rs.getInt("stock_id"));
        h.setTotalQuantity(rs.getInt("total_quantity"));
        h.setReservedQuantity(rs.getInt("reserved_quantity"));
        return h;
    }
}