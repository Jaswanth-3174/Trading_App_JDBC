package model;

import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int userId;
    private int stockId;
    private String stockName;  // For display
    private String username;   // For display
    private int originalQuantity;
    private int quantity;
    private double price;
    private boolean isBuy;
    private String status;  // OPEN, PARTIAL, FILLED, CANCELLED
    private Timestamp createdAt;
    
    public Order() {}
    
    public int getFilledQuantity() {
        return originalQuantity - quantity;
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }
    
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getOriginalQuantity() { return originalQuantity; }
    public void setOriginalQuantity(int originalQuantity) { this.originalQuantity = originalQuantity; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public boolean isBuy() { return isBuy; }
    public void setBuy(boolean buy) { isBuy = buy; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}