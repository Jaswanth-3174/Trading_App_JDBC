package dao;

import dbOperations.*;
import trading.Stock;

import java.sql.SQLException;
import java.util.*;

public class StockDAO {

    private static String tableName = "stocks";

    public Stock findById(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToStock(rows.get(0)) : null;
    }

    public Stock findByName(String stockName) throws SQLException {
        Condition c = new Condition();
        c.add("stock_name", stockName);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToStock(rows.get(0)) : null;
    }

    public static int getStockIdByName(String stockName) throws SQLException {
        Condition c = new Condition();
        c.add("stock_name", stockName);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"stock_id"}, c);
        return !rows.isEmpty() ? ((Number) rows.get(0).get("stock_id")).intValue() : -1;
    }

    public static String getStockNameById(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"stock_id"}, c);
        return !rows.isEmpty() ? (String) rows.get(0).get("stock_name") : null;
    }

    public Stock createStock(String stockName) throws SQLException {
        Condition data = new Condition();
        data.add("stock_name", stockName);
        int stockId = InsertOperation.insert(tableName, data);
        return stockId > 0 ? findById(stockId) : null;
    }

    public boolean deleteStock(int stockId) throws SQLException {
        Condition c = new Condition();
        c.add("stock_id", stockId);
        int affected = DeleteOperation.delete(tableName, c);
        return affected > 0;
    }

    public List<Stock> listAllStocks() throws SQLException {
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, null);
        List<Stock> stocks = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            stocks.add(mapToStock(row));
        }
        return stocks;
    }

    private Stock mapToStock(HashMap<String, Object> row) {
        Stock stock = new Stock();
        stock.setStockId(((Number) row.get("stock_id")).intValue());
        stock.setStockName((String) row.get("stock_name"));
        return stock;
    }
}
