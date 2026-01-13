package dao;

import dbOperations.*;
import trading.Transaction;

import java.sql.SQLException;
import java.util.*;

public class TransactionDAO {

    private final String table = "transactions";
    private final String tableAlias = "transactions t";

    private final String joinCondition = "JOIN stocks s ON t.stock_id = s.stock_id " +
            "JOIN users buy ON t.buyer_id = buy.user_id " +
            "JOIN users sel ON t.seller_id = sel.user_id";

    private final String[] columns = {
            "t.transactions_id", "t.buyer_id", "t.seller_id", "t.stock_id", "t.quantity", "t.price",
            "s.stock_name", "buy.username AS buyer_name", "sel.username AS seller_name"
    };

    public Transaction createTransaction(int buyerId, int sellerId,
                                         int stockId, int quantity, double price) throws SQLException {
        Condition data = new Condition();
        data.add("buyer_id", buyerId);
        data.add("seller_id", sellerId);
        data.add("stock_id", stockId);
        data.add("quantity", quantity);
        data.add("price", price);

        int transId = InsertOperation.insert(table, data);
        return transId > 0 ? findById(transId) : null;
    }

    public Transaction findById(int transactionId) throws SQLException {
        Condition where = new Condition();
        where.add("t.transactions_id", transactionId);

        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, columns, joinCondition, where, null);
        return rows.isEmpty() ? null : mapToTransaction(rows.get(0));
    }

    public List<Transaction> findByUserId(int userId) throws SQLException {
        SpecialCondition extra = new SpecialCondition("t.buyer_id = ? OR t.seller_id = ?",
                userId, userId);
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithAdvancedCondition(tableAlias,
                        columns, joinCondition, null,extra, null, 0);
        return mapToRowList(rows);
    }

    public List<Transaction> findAll() throws SQLException {
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, columns, joinCondition,
                        null, null);
        return mapToRowList(rows);
    }

    private Transaction mapToTransaction(HashMap<String, Object> row) throws SQLException{
        Transaction t = new Transaction();
        t.setTransactionId(((Number) row.get("transactions_id")).intValue());
        t.setBuyerId(((Number) row.get("buyer_id")).intValue());
        t.setSellerId(((Number) row.get("seller_id")).intValue());
        t.setStockId(((Number) row.get("stock_id")).intValue());
        t.setQuantity(((Number) row.get("quantity")).intValue());
        t.setPrice(((Number) row.get("price")).doubleValue());
        return t;
    }

    private List<Transaction> mapToRowList(ArrayList<HashMap<String, Object>> rows) throws SQLException{
        List<Transaction> transactions = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            transactions.add(mapToTransaction(row));
        }
        return transactions;
    }
}