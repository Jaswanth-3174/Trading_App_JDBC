package dao;

import dbOperations.*;
import trading.StockHolding;

import java.sql.SQLException;
import java.util.*;

public class StockHoldingDAO {

    private final String table = "stock_holdings";
    private final String tableAlias = "stock_holdings h";
    private final String joinCondition = "JOIN stocks s ON h.stock_id = s.stock_id";

    private final String[] columns = {"h.holding_id", "h.demat_id", "h.stock_id",
            "h.total_quantity", "h.reserved_quantity", "s.stock_name"
    };

    public StockHolding findById(int holdingId) throws SQLException {
        Condition c = new Condition();
        c.add("holding_id", holdingId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(table, c);
        return rows.isEmpty() ? null : mapToRow(rows.get(0));
    }

    public List<StockHolding> findByDematId(int dematId) throws SQLException {
        Condition where = new Condition();
        where.add("h.demat_id", dematId);
        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, columns, joinCondition, where, null);
        return mapToRowList(rows);
    }

    public StockHolding findByDematAndStock(int dematId, int stockId) throws SQLException {
        Condition where = new Condition();
        where.add("h.demat_id", dematId);
        where.add("h.stock_id", stockId);

        ArrayList<HashMap<String, Object>> rows =
                SelectOperation.selectWithJoin(tableAlias, columns, joinCondition, where, null);
        return rows.isEmpty() ? null : mapToRow(rows.get(0));
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

        return UpdateOperation.update(table, set, where) > 0;
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

        return UpdateOperation.update(table, set, where) > 0;
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

        return UpdateOperation.update(table, set, where) > 0;
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

        return UpdateOperation.update(table, set, where) > 0;
    }

    public StockHolding createHolding(int dematId, int stockId, int quantity) throws SQLException {
        Condition data = new Condition();
        data.add("demat_id", dematId);
        data.add("stock_id", stockId);
        data.add("total_quantity", quantity);
        data.add("reserved_quantity", 0);

        int holdingId = InsertOperation.insert(table, data);
        return holdingId > 0 ? findById(holdingId) : null;
    }

    public boolean deleteHolding(int holdingId) throws SQLException {
        Condition c = new Condition();
        c.add("holding_id", holdingId);
        return DeleteOperation.delete(table, c) > 0;
    }

    private StockHolding mapToRow(HashMap<String, Object> row) {
        StockHolding stockHolding = new StockHolding();
        stockHolding.setStockHoldingId(((Number) row.get("holding_id")).intValue());
        stockHolding.setDematId(((Number) row.get("demat_id")).intValue());
        stockHolding.setStockId(((Number) row.get("stock_id")).intValue());
        stockHolding.setTotalQuantity(((Number) row.get("total_quantity")).intValue());
        stockHolding.setReservedQuantity(((Number) row.get("reserved_quantity")).intValue());
        return stockHolding;
    }

    private List<StockHolding> mapToRowList(ArrayList<HashMap<String, Object>> rows) {
        List<StockHolding> stockHoldings = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            stockHoldings.add(mapToRow(row));
        }
        return stockHoldings;
    }
}