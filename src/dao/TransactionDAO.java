package dao;

import dbConnection.DatabaseConfig;
import dbOperations.Condition;
import dbOperations.InsertOperation;
import dbOperations.SelectOperation;
import trading.Transaction;
import java.sql.*;
import java.util.*;

public class TransactionDAO {

    public String tableName = "transactions";
    public String[] columns = {"t.*", "s.stock_name", "buy.username as buyer_name",
                                "sel.username as seller_name"};
    public String joinCondition = "t JOIN stocks s ON t.stock_id = s.stock_id JOIN users buy ON t.buyer_id = buy.user_id JOIN users sel ON t.seller_id = sel.user_id";

    public Transaction createTransaction(int buyerId, int sellerId, int stockId, int quantity, double price) throws SQLException {
        Condition data = new Condition();
        data.add("buyer_id", buyerId);
        data.add("seller_id", sellerId);
        data.add("stock_id", stockId);
        data.add("quantity", quantity);
        data.add("price", price);
        int transId = InsertOperation.insert(tableName, data);
        return transId > 0 ? findById(transId) : null;
    }

    public Transaction findById(int transactionId) throws SQLException {
        Condition where = new Condition();
        where.add("t.transactions_id", transactionId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, columns, joinCondition, where, null
        );
        return !rows.isEmpty() ? mapToOrder(rows.get(0)) : null;
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
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, columns, joinCondition, null, null
        );
        return mapToRowList(rows);
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

    private Transaction mapToOrder(HashMap<String, Object> row) throws SQLException{
        Transaction t = new Transaction();
        t.setTransactionId(((Number)row.get("transactions_id")).intValue());
        t.setBuyerId(((Number)row.get("buyer_id")).intValue());
        t.setSellerId(((Number)row.get("seller_id")).intValue());
        t.setStockId(((Number)row.get("stock_id")).intValue());
        t.setQuantity(((Number)row.get("quantity")).intValue());
        t.setPrice(((Number)row.get("price")).doubleValue());
        return t;
    }

    private List<Transaction> mapToRowList(ArrayList<HashMap<String, Object>> rows) throws SQLException{
        List<Transaction> transactions = new ArrayList<>();
        for(HashMap<String, Object> row : rows){
            transactions.add(mapToOrder(row));
        }
        return transactions;
    }
}