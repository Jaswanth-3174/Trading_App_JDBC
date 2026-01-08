package dao;

import dbConnection.DatabaseConfig;
import trading.*;
import dbOperations.*;

import java.sql.*;
import java.util.*;

public class OrderDAO {

    private String tableName = "orders o";
    private String joinCondition = "o JOIN stocks s ON o.stock_id = s.stock_id";
    private String[] COLUMNS = {"o.*", "s.stock_name"};
    private int size = 10; // limit 10

    public Order findById(int orderId) throws SQLException {
        Condition c = new Condition();
        c.add("o.order_id", orderId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, COLUMNS, joinCondition, c, null
        );
        return !rows.isEmpty() ? mapToOrder(rows.get(0)) : null;
    }

    public Order createOrder(int userId, String stockName,
                             int quantity, double price, boolean isBuy) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        if (stockId < 0) {
            throw new SQLException("Stock not found: " + stockName);
        }
        Condition data = new Condition();
        data.add("user_id", userId);
        data.add("stock_id", stockId);
        data.add("quantity", quantity);
        data.add("price", price);
        data.add("is_buy", isBuy);
        int orderId = InsertOperation.insert(tableName, data);
        return orderId > 0 ? findById(orderId) : null;
    }

    public Order mapToOrder(HashMap<String, Object> row) throws SQLException {
        Order order = new Order();
        order.setOrderId(((Number) row.get("order_id")).intValue());
        order.setUserId(((Number) row.get("user_id")).intValue());
        order.setStockId(((Number) row.get("stock_id")).intValue());
        order.setQuantity(((Number) row.get("quantity")).intValue());
        order.setPrice(((Number) row.get("price")).doubleValue());
        order.setBuy((Boolean) row.get("is_buy"));
        return order;
    }

    private List<Order> mapToOrderList(ArrayList<HashMap<String, Object>> rows) throws SQLException {
        List<Order> orders = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            orders.add(mapToOrder(row));
        }
        return orders;
    }

    public List<Order> getBuyOrders(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        c.add("is_buy", true);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, null, null, c, "price DESC, order_id ASC", size
        );
        return mapToOrderList(rows);
    }

    public List<Order> getSellOrders(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        c.add("is_buy", false);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, null, null, c, "price ASC, order_id ASC", size
        );
        return mapToOrderList(rows);
    }

    public List<Order> getNextBuyOrders(int stockId, double lastPrice, int lastOrderId) throws SQLException {
        String sql = """
            SELECT * FROM orders 
            WHERE stock_id = ? AND is_buy = TRUE 
            AND (price < ? OR (price = ? AND order_id > ?))
            ORDER BY price DESC, order_id ASC 
            LIMIT ?
            """;
        return executeWithCursor(sql, stockId, lastPrice, lastPrice, lastOrderId, size);
    }

    public List<Order> getNextSellOrders(int stockId, double lastPrice, int lastOrderId) throws SQLException {
        String sql = """
            SELECT * FROM orders 
            WHERE stock_id = ? AND is_buy = FALSE 
            AND (price > ? OR (price = ? AND order_id > ?))
            ORDER BY price ASC, order_id ASC 
            LIMIT ?
            """;
        return executeWithCursor(sql, stockId, lastPrice, lastPrice, lastOrderId, size);
    }

    public Order findMatchingOrder(int stockId, boolean isBuy, int excludeUserId, double priceThreshold) throws SQLException {
        String sql;
        if (isBuy) {
            // look for buy orders with price >= threshold
            sql = """
                SELECT * FROM orders 
                WHERE stock_id = ? AND is_buy = TRUE AND user_id != ? AND price >= ?
                ORDER BY price DESC, order_id ASC 
                LIMIT 1
                """;
        } else {
            // look for sell orders with price <= threshold
            sql = """
                SELECT * FROM orders 
                WHERE stock_id = ? AND is_buy = FALSE AND user_id != ? AND price <= ?
                ORDER BY price ASC, order_id ASC 
                LIMIT 1
                """;
        }

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, stockId);
            ps.setInt(2, excludeUserId);
            ps.setDouble(3, priceThreshold);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapToOrderFromResultSet(rs);
            }
        }
        return null;
    }

    // Helper: Execute cursor-based query
    private List<Order> executeWithCursor(String sql, Object... params) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(mapToOrderFromResultSet(rs));
            }
        }
        return orders;
    }

    // user specific orders
    public List<Order> findByUserId(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("o.user_id", userId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, COLUMNS, joinCondition, c, "o.order_id"
        );
        return mapToOrderList(rows);
    }

    // updates only quantity
    public boolean updateQuantity(int orderId, int newQuantity) throws SQLException {
        if (newQuantity <= 0) {
            return cancelOrder(orderId);
        }
        Condition set = new Condition();
        set.add("quantity", newQuantity);
        Condition where = new Condition();
        where.add("order_id", orderId);
        int affected = UpdateOperation.update(tableName, set, where);
        return affected > 0;
    }

    // updates both quantity and price
    public boolean modifyOrder(int orderId, int newQuantity, double newPrice) throws SQLException {
        if (newQuantity <= 0){
            return cancelOrder(orderId);
        }
        Condition set = new Condition();
        set.add("quantity", newQuantity);
        set.add("price", newPrice);
        Condition where = new Condition();
        where.add("order_id", orderId);
        int affected = UpdateOperation.update(tableName, set, where);
        return affected > 0;
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        Condition where = new Condition();
        where.add("order_id", orderId);
        int affected = DeleteOperation.delete(tableName, where);
        return affected > 0;
    }

    private Order mapToOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setStockId(rs.getInt("stock_id"));
        order.setQuantity(rs.getInt("quantity"));
        order.setPrice(rs.getDouble("price"));
        order.setBuy(rs.getBoolean("is_buy"));
        return order;
    }
}