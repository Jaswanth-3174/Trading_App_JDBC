package dao;

import dbConnection.DatabaseConfig;
import trading.Transaction;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    private StockDAO stockDAO = new StockDAO();

    public Transaction createTransaction(int buyerId, int sellerId, int stockId, int quantity, double price) throws SQLException {
        String sql = """
            INSERT INTO transactions (buyer_id, seller_id, stock_id, quantity, price) 
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, buyerId);
            stmt.setInt(2, sellerId);
            stmt.setInt(3, stockId);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, price);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }

    public Transaction findById(int transactionId) throws SQLException {
        String sql = """
            SELECT t.*, s.stock_name, 
                   buy.username as buyer_name, 
                   sel.username as seller_name
            FROM transactions t 
            JOIN stocks s ON t.stock_id = s.stock_id 
            JOIN users buy ON t.buyer_id = buy.user_id
            JOIN users sel ON t.seller_id = sel.user_id
            WHERE t.transactions_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return printRow(rs);
            }
        }
        return null;
    }

    // user specific transactions
    public List<Transaction> findByUserId(int userId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
            SELECT t.*, s.stock_name, 
                   buy.username as buyer_name,
                   sel.username as seller_name
            FROM transactions t 
            JOIN stocks s ON t.stock_id = s.stock_id 
            JOIN users buy ON t.buyer_id = buy.user_id
            JOIN users sel ON t.seller_id = sel.user_id
            WHERE t.buyer_id = ? OR t.seller_id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(printRow(rs));
            }
        }
        return transactions;
    }

    // all transactions
    public List<Transaction> findAll() throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        String sql = """
            SELECT t.*, s.stock_name, 
                   b.username as buyer_name, 
                   sel.username as seller_name
            FROM transactions t 
            JOIN stocks s ON t.stock_id = s.stock_id 
            JOIN users b ON t.buyer_id = b.user_id
            JOIN users sel ON t.seller_id = sel.user_id
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(printRow(rs));
            }
        }
        return transactions;
    }

    private Transaction printRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transactions_id"));
        t.setBuyerId(rs.getInt("buyer_id"));
        t.setSellerId(rs.getInt("seller_id"));
        t.setStockId(rs.getInt("stock_id"));
        t.setQuantity(rs.getInt("quantity"));
        t.setPrice(rs.getDouble("price"));
        return t;
    }
}