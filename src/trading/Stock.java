package trading;

public class Stock {
    private int stockId;
    private String stockName;

    public Stock() {}

    public Stock(int stockId, String stockName) {
        this.stockId = stockId;
        this.stockName = stockName;
    }

    // Getters and Setters
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    @Override
    public String toString() {
        return stockName;
    }
}
