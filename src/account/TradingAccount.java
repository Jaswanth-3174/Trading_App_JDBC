package account;

public class TradingAccount {
    private int tradingAccountId;
    private int userId;
    private double balance;
    private double reservedBalance;

    public TradingAccount() {}

    public int getTradingAccountId() { return tradingAccountId; }
    public void setTradingAccountId(int tradingAccountId) { this.tradingAccountId = tradingAccountId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getReservedBalance() { return reservedBalance; }
    public void setReservedBalance(double reservedBalance) { this.reservedBalance = reservedBalance; }

    public double getAvailableBalance() {
        return balance;
    }

    public double getTotalBalance() {
        return balance + reservedBalance;
    }

    @Override
    public String toString() {
        return String.format("Trading Account: Available=Rs.%.2f, Reserved=Rs.%.2f, Total=Rs.%.2f",
                balance, reservedBalance, getTotalBalance());
    }
}