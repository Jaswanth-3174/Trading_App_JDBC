package trading;

import dao.StockDAO;

import java.sql.SQLException;

public class StockHolding {
    private int stockHoldingId;
    private int dematId;
    private int stockId;
    private int totalQuantity;
    private int reservedQuantity;
    private String stockName;

    public StockHolding(){

    }

    public StockHolding(String stockName) {
        this.totalQuantity = 0;
        this.reservedQuantity = 0;
    }

    public int getStockId(){
        return stockId;
    }
    public void setStockId(int id){
        this.stockId = id;
    }

    public int getStockHoldingId() {
        return stockHoldingId;
    }
    public void setStockHoldingId(int stockHoldingId) {
        this.stockHoldingId = stockHoldingId;
    }

    public int getDematId() {
        return dematId;
    }
    public void setDematId(int dematId) {
        this.dematId = dematId;
    }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }

    public String getStockName() throws SQLException {
        return StockDAO.getStockNameById(this.stockId);
    }
}