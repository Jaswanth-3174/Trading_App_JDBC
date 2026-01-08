package dao;

import account.DematAccount;
import dbConnection.DatabaseConfig;
import dbOperations.Condition;
import dbOperations.*;
import trading.Order;
import trading.StockHolding;

import java.sql.*;
import java.util.*;

public class StockHoldingDAO {

    public String tableName = "stock_holdings h";
    public String joinCondition = "h JOIN stocks s ON h.stock_id = s.stock_id";
    public String[] columns = {"h.*", "s.stock_name"};

    public StockHolding findById(int holdingId) throws SQLException {
        Condition c = new Condition();
        c.add("holding_id", holdingId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToRow(rows.get(0)) : null;
    }

    public List<StockHolding> findByDematId(int dematId) throws SQLException {
        Condition where = new Condition();
        where.add("h.demat_id", dematId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, columns, joinCondition, where, null
        );
        return mapToRowList(rows);
    }

    public StockHolding findByDematAndStock(int dematId, int stockId) throws SQLException {
        Condition where = new Condition();
        where.add("h.demat_id", dematId);
        where.add("h.stock_id", stockId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.selectWithJoin(
                tableName, columns, joinCondition, where, null
        );
        return !rows.isEmpty() ? mapToRow(rows.get(0)) : null;
    }

    public boolean reserveStocks(int dematId, int stockId, int quantity) throws SQLException {
        StockHolding holding = findByDematAndStock(dematId, stockId);
        if (holding == null || holding.getAvailableQuantity() < quantity) {
            return false;
        }
        Condition set = new Condition();
        set.add("reserved_quantity", holding.getReservedQuantity() + quantity);
        Condition where = new Condition();
        where.add("holding_id", holding.getStockHoldingId());
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean releaseReservedStocks(int dematId, int stockId, int quantity) throws SQLException {
        StockHolding holding = findByDematAndStock(dematId, stockId);
        if (holding == null || holding.getReservedQuantity() < quantity) {
            return false;
        }
        Condition set = new Condition();
        set.add("reserved_quantity", holding.getReservedQuantity() - quantity);
        Condition where = new Condition();
        where.add("holding_id", holding.getStockHoldingId());
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean sellShares(int dematId, int stockId, int quantity) throws SQLException {
        StockHolding holding = findByDematAndStock(dematId, stockId);
        if (holding == null || holding.getReservedQuantity() < quantity) {
            return false;
        }
        int newTotal = holding.getTotalQuantity() - quantity;
        int newReserved = holding.getReservedQuantity() - quantity;

        if (newTotal <= 0) {
            return deleteHolding(holding.getStockHoldingId());
        }

        Condition set = new Condition();
        set.add("total_quantity", newTotal);
        set.add("reserved_quantity", newReserved);
        Condition where = new Condition();
        where.add("holding_id", holding.getStockHoldingId());
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean addShares(int dematId, int stockId, int quantity) throws SQLException {
        StockHolding holding = findByDematAndStock(dematId, stockId);
        if (holding == null) {
            return createHolding(dematId, stockId, quantity) != null;
        }
        Condition set = new Condition();
        set.add("total_quantity", holding.getTotalQuantity() + quantity);
        Condition where = new Condition();
        where.add("holding_id", holding.getStockHoldingId());
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean deleteHolding(int holdingId) throws SQLException {
        Condition c = new Condition();
        c.add("holding_id", holdingId);
        int affected = DeleteOperation.delete(tableName, c);
        return affected > 0;
    }

    public StockHolding createHolding(int dematId, int stockId, int quantity) throws SQLException {
        Condition data = new Condition();
        data.add("demat_id", dematId);
        data.add("stock_id", stockId);
        data.add("total_quantity", quantity);
        data.add("reserved_quantity", 0);
        int holdingId = InsertOperation.insert(tableName, data);
        return holdingId > 0 ? findById(holdingId) : null;
    }

//    public boolean reserveStocks(int dematId, int stockId, int quantity) throws SQLException {
//        StockHolding stockHolding = findByDematAndStock(dematId, stockId);
//        Condition set = new Condition();
//        set.add("reserved_quantity", stockHolding.getReservedQuantity() + quantity);
//        Condition where = new Condition();
//        where.add("demat_id", dematId);
//        where.add("stock_id", stockId);
//        where.add("(total_quantity - reserved_quantity) >", quantity);
//        return UpdateOperation.update(tableName, set, where) > 0;
//    }

//    public boolean releaseReservedStocks(int dematId, int stockId, int quantity) throws SQLException {
//        StockHolding stockHolding = findByDematAndStock(dematId, stockId);
//        Condition set = new Condition();
//        set.add("reserved_quantity", stockHolding.getReservedQuantity() - quantity);
//        Condition where = new Condition();
//        where.add("demat_id", dematId);
//        where.add("stock_id", stockId);
//        where.add("reserved_quantity >", quantity);
//        return UpdateOperation.update(tableName, set, where) > 0;
//    }
//
//    public boolean sellShares(int dematId, int stockId, int quantity) throws SQLException {
//        StockHolding stockHolding = findByDematAndStock(dematId, stockId);
//        Condition set = new Condition();
//        set.add("total_quantity", stockHolding.getTotalQuantity() - quantity);
//        set.add("reserved_quantity", stockHolding.getReservedQuantity() - quantity);
//        Condition where = new Condition();
//        where.add("demat_id", dematId);
//        where.add("stock_id", stockId);
//        where.add("reserved_quantity >", quantity);
//        return UpdateOperation.update(tableName, set, where) > 0;
//    }
//
//    public boolean addShares(int dematId, int stockId, int quantity) throws SQLException {
//        StockHolding stockHolding = findByDematAndStock(dematId, stockId);
//        Condition set = new Condition();
//        set.add("total_quantity", stockHolding.getTotalQuantity() + quantity);
//        Condition where = new Condition();
//        where.add("demat_id", dematId);
//        where.add("stock_id", stockId);
//        int affected = UpdateOperation.update(tableName, set, where);
//        if(affected > 0) {
//            return true;
//        }
//        Condition data = new Condition();
//        data.add("demat_id", dematId);
//        data.add("stock_id", stockId);
//        data.add("total_quantity", quantity);
//        data.add("reserved_quantity", 0);
//        int insertedId = InsertOperation.insert(tableName, data);
//        return insertedId > 0;
//    }

    private StockHolding mapToRow(HashMap<String, Object> row){
        StockHolding stockHolding = new StockHolding();
        stockHolding.setStockHoldingId(((Number) row.get("holding_id")).intValue());
        stockHolding.setDematId(((Number)row.get("demat_id")).intValue());
        stockHolding.setStockId(((Number)row.get("stock_id")).intValue());
        stockHolding.setTotalQuantity(((Number)row.get("total_quantity")).intValue());
        stockHolding.setReservedQuantity(((Number)row.get("reserved_quantity")).intValue());
        return stockHolding;
    }

    private List<StockHolding> mapToRowList(ArrayList<HashMap<String, Object>> rows) throws SQLException {
        List<StockHolding> stockHoldings = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            stockHoldings.add(mapToRow(row));
        }
        return stockHoldings;
    }
}