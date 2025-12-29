package model;

public class StockHolding {
    private int holdingId;
    private int dematId;
    private int stockId;
    private String stockName;  // For display
    private int totalQuantity;
    private int reservedQuantity;
    
    public StockHolding() {}
    
    public int getAvailableQuantity() {
        return totalQuantity - reservedQuantity;
    }
    
    // Getters and Setters
    public int getHoldingId() { return holdingId; }
    public void setHoldingId(int holdingId) { this.holdingId = holdingId; }
    
    public int getDematId() { return dematId; }
    public void setDematId(int dematId) { this.dematId = dematId; }
    
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    
    public int getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity = reservedQuantity; }
}