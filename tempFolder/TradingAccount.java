package model;

public class TradingAccount {
    private int tradingId;
    private int userId;
    private double balance;
    private double reservedBalance;
    
    public TradingAccount() {}
    
    public TradingAccount(int userId, double balance) {
        this.userId = userId;
        this.balance = balance;
        this.reservedBalance = 0.0;
    }
    
    public double getAvailableBalance() {
        return balance - reservedBalance;
    }
    
    // Getters and Setters
    public int getTradingId() { return tradingId; }
    public void setTradingId(int tradingId) { this.tradingId = tradingId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    
    public double getReservedBalance() { return reservedBalance; }
    public void setReservedBalance(double reservedBalance) { this.reservedBalance = reservedBalance; }
}