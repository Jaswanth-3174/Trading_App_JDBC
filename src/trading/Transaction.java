package trading;

import dao.UserDAO;

import java.sql.SQLException;

public class Transaction {
    private int transactionId;
    private int buyerId;
    private int sellerId;
    private int stockId;
    private int quantity;
    private double price;

    private String stockName;
    private double total;
    private String userName;

    public Transaction() throws SQLException {}

    public String getUserName(int id) throws SQLException{
        return UserDAO.findUsernameById(id);
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public int getStockId(){
        return this.stockId;
    }
    public void setStockId(int stockId){
        this.stockId = stockId;
    }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    String buyerName = UserDAO.findUsernameById(getBuyerId());
    String sellerName = UserDAO.findUsernameById(getSellerId());

    @Override
    public String toString() {
        return String.format("Transaction #%d: %s -> %s | %s x%d @ Rs.%.2f = Rs.%.2f",
                transactionId, sellerName, buyerName, sellerName, quantity, price, total);
    }
}