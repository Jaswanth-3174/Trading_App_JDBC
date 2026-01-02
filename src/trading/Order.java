package trading;

import account.*;
import dao.StockDAO;

import java.sql.SQLException;

public class Order {
    private int orderId;
    private int userId;
    private int stockId;
    private int quantity;
    private double price;
    private boolean isBuy;
    private double total;

    public Order() throws SQLException {
    }

    String stockName = getStockName();

    public Order(int userId, int stockId, int quantity, double price, boolean isBuy) throws SQLException {
        this.userId = userId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.price = price;
        this.isBuy = isBuy;
        this.total = price * quantity;
    }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int id){
        this.orderId = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int id){
        this.userId = id;
    }

    public int getStockId(){
        return this.stockId;
    }
    public void setStockId(int id){
        this.stockId = id;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price){
        this.price = price;
    }

    public boolean isBuy() {
        return isBuy;
    } // get isBuy
    public void setBuy(boolean b){
        this.isBuy = b;
    }

    public double getTotal(){
        return this.total = getPrice() * getQuantity();
    }

    public String getStockName() throws SQLException{
        return StockDAO.getStockNameById(stockId);
    }

    @Override
    public String toString() {
        return String.format("Order #%d [%s %s: %d @ Rs.%.2f]",
                orderId, isBuy ? "BUY" : "SELL", stockName , quantity, price);
    }

}