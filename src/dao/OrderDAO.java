package dao;

import dbConnection.DatabaseConfig;
import trading.*;
import dbOperations.*;

import java.sql.*;
import java.util.*;

public class OrderDAO {

    private String tableName = "orders o";
    private String joinCondition = "JOIN stocks s ON o.stock_id = s.stock_id";
    private String[] COLUMNS = {"o.*", "s.stock_name"};

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

    // all buy orders
    public List<Order> getBuyOrders(String stockName) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        Condition c = new Condition();
        c.add("o.stock_id", stockId);
        c.add("o.is_buy", "TRUE");
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, COLUMNS, joinCondition, c, "o.price DESC, o.order_id ASC"
        );
        return mapToOrderList(rows);
    }

    // all sell orders
    public List<Order> getSellOrders(String stockName) throws SQLException {
        int stockId = StockDAO.getStockIdByName(stockName);
        Condition c = new Condition();
        c.add("o.stock_id", stockId);
        c.add("o.is_buy", "FALSE");
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, COLUMNS, joinCondition, c, "o.price ASC, o.order_id ASC"
        );
        return mapToOrderList(rows);
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
}