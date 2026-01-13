package dao;

import trading.*;
import dbOperations.*;

import java.sql.*;
import java.util.*;

public class OrderDAO {
    private String table = "orders";          // for insert, update, delete
    private String tableAlias = "orders o";   // for select with join
    private String joinCondition = "JOIN stocks s ON o.stock_id = s.stock_id";

    private final String[] COLUMNS = {"o.order_id", "o.user_id", "o.stock_id", "o.quantity",
            "o.price", "o.is_buy", "s.stock_name"};

    private int size = 10; // for getting orders sell/buy

    public Order findById(int orderId) throws SQLException {
        Condition c = new Condition();
        c.add("o.order_id", orderId);
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, COLUMNS, joinCondition, c, null);
        return rows.isEmpty() ? null : mapToOrder(rows.get(0));
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
        int orderId = InsertOperation.insert(table, data);
        return orderId > 0 ? findById(orderId) : null;
    }

    public int cancelAllOrdersByUserId(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("user_id", userId);
        return DeleteOperation.delete(table, c);
    }

    public List<Order> getBuyOrders(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        c.add("is_buy", true);
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(table, null, null, c,
                        "price DESC, order_id ASC", size);
        return mapToOrderList(rows);
    }

    public List<Order> getSellOrders(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        c.add("is_buy", false);
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(table, null, null, c,
                        "price ASC, order_id ASC", size);
        return mapToOrderList(rows);
    }

    public List<Order> getNextBuyOrders(int stockId, double lastPrice, int lastOrderId)
            throws SQLException {
        Condition base = new Condition();
        base.add("stock_id", stockId);
        base.add("is_buy", true);
        SpecialCondition cursor = new SpecialCondition(
                "price < ? OR (price = ? AND order_id > ?)",
                lastPrice, lastPrice, lastOrderId
        );
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithAdvancedCondition(
                        table, null, null, base, cursor,
                        "price DESC, order_id ASC", size);
        return mapToOrderList(rows);
    }

    public List<Order> getNextSellOrders(int stockId, double lastPrice, int lastOrderId)
            throws SQLException {
        Condition base = new Condition();
        base.add("stock_id", stockId);
        base.add("is_buy", false);
        SpecialCondition cursor = new SpecialCondition(
                "price > ? OR (price = ? AND order_id > ?)",
                lastPrice, lastPrice, lastOrderId
        );
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithAdvancedCondition(table, null, null, base,
                        cursor, "price ASC, order_id ASC", size);
        return mapToOrderList(rows);
    }

//    public Order findMatchingOrder(int stockId, boolean isBuy, int excludeUserId,
//                                   double priceThreshold) throws SQLException {
//
//        Condition base = new Condition();
//        base.add("stock_id", stockId);
//        base.add("is_buy", isBuy);
//
//        SpecialCondition extra;
//        String orderBy;
//
//        if (isBuy) {
//            extra = new SpecialCondition("user_id != ? AND price >= ?", excludeUserId, priceThreshold);
//            orderBy = "price DESC, order_id ASC";
//        } else {
//            extra = new SpecialCondition("user_id != ? AND price <= ?", excludeUserId, priceThreshold);
//            orderBy = "price ASC, order_id ASC";
//        }
//
//        ArrayList<HashMap<String, Object>> rows =
//                SelectOperation.selectWithAdvancedCondition(table, null, null,
//                        base, extra, orderBy, 1);
//
//        return rows.isEmpty() ? null : mapToOrder(rows.get(0));
//    }

    public List<Order> findByUserId(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("o.user_id", userId);

        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, COLUMNS, joinCondition, c,
                        "o.order_id");

        return mapToOrderList(rows);
    }

    public boolean updateQuantity(int orderId, int newQuantity) throws SQLException {
        if (newQuantity <= 0) {
            return cancelOrder(orderId);
        }

        Condition set = new Condition();
        set.add("quantity", newQuantity);

        Condition where = new Condition();
        where.add("order_id", orderId);

        return UpdateOperation.update(table, set, where) > 0;
    }

    public boolean modifyOrder(int orderId, int newQuantity, double newPrice) throws SQLException {
        if (newQuantity <= 0) {
            return cancelOrder(orderId);
        }

        Condition set = new Condition();
        set.add("quantity", newQuantity);
        set.add("price", newPrice);

        Condition where = new Condition();
        where.add("order_id", orderId);

        return UpdateOperation.update(table, set, where) > 0;
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        Condition where = new Condition();
        where.add("order_id", orderId);
        return DeleteOperation.delete(table, where) > 0;
    }

    private Order mapToOrder(HashMap<String, Object> row) throws SQLException{
        Order order = new Order();
        order.setOrderId(((Number) row.get("order_id")).intValue());
        order.setUserId(((Number) row.get("user_id")).intValue());
        order.setStockId(((Number) row.get("stock_id")).intValue());
        order.setQuantity(((Number) row.get("quantity")).intValue());
        order.setPrice(((Number) row.get("price")).doubleValue());
        order.setBuy((Boolean) row.get("is_buy"));
        return order;
    }

    private List<Order> mapToOrderList(ArrayList<HashMap<String, Object>> rows) throws SQLException{
        List<Order> orders = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            orders.add(mapToOrder(row));
        }
        return orders;
    }
}