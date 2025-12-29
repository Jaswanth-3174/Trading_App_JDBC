package account;

import trading.Transaction;

import java.util.ArrayList;

public class TradingAccount {
    private static int idCounter = 1;

    private int tradingAccountId;
    private int userId;
    private double balance;
    private double reservedBalance;
    private double currentBalance;
    private ArrayList<Integer> orderIds;
    private ArrayList<Transaction> transactions;
    private boolean isActive;


    public TradingAccount(){
    }

    public TradingAccount(int userId){
        this.tradingAccountId = idCounter++;
        this.userId = userId;
        this.balance = fetchBalance();
        this.reservedBalance = 0;
        this.currentBalance = balance;
        this.orderIds = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.isActive = true;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    private double fetchBalance(){   // returns random amount between 1000 to 5000
        int min = 1000;
        int max = 5000;
        double randomNumber = (int)(Math.random() * (max - min + 1)) + min;
        return randomNumber;
    }

    public int getTradingAccountId() {
        return this.tradingAccountId;
    }

    public void setTradingAccountId(int id){
        this.tradingAccountId = id;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int id){
        this.userId = id;
    }

    public double getBalance() {
        return this.balance;
    }

    public void setBalance(double balance){
        this.balance = balance;
    }

    public double getReservedBalance() {
        return this.reservedBalance;
    }

    public void setReservedBalance(double balance){
        this.reservedBalance = balance;
    }

    public double getCurrentBalance() {
        return this.currentBalance;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public ArrayList<Integer> getOrderIds(){
        return new ArrayList<>(orderIds);
    }

    public boolean reserveBalance(double amount){
        if(amount > currentBalance){
            System.out.println("Insufficient funds!");
            return false;
        }
        currentBalance -= amount;
        reservedBalance += amount;
        System.out.println("Amount reserved : " + reservedBalance);
        showBalances();
        return true;
    }

    public boolean releaseReservedBalance(double amount){
        if(amount > reservedBalance){
            System.out.println("Insufficient funds!");
            return false;
        }
        currentBalance += amount;
        reservedBalance -= amount;
        return true;
    }

    public void showBalances(){
        System.out.println("\nBalance : ");
        System.out.println("Total : " + balance + " | Current Balance : " + currentBalance + " | Reserved Balance : " + reservedBalance+"\n");
    }

    public void credit(double amount){
        balance += amount;
        currentBalance += amount;
    }

    public void debit(double amount){
        if(amount > reservedBalance){
            System.out.println("Cannot debit, amount not reserved!");
            return;
        }
        balance -= amount;
        reservedBalance -= amount;
    }

    public void addOrder(int id){
        orderIds.add(id);
    }

    //    public void addTransaction(trading.Transaction transaction){
    //        transactions.add(transaction);
    //    }
    //
    //    public void displayTransactions(){
    //        System.out.println("+----------+----------+----------+----------+---------------+---------------+----------+----------+---------------+");
    //        System.out.printf(
    //                "|%-12s|%-10s|%-10s|%-12s|%-14s|%-10s|%-8s|%-12s|%-14s|%-15s|\n",
    //                "Trans ID", "Buyer ID", "Seller ID", "Buyer Trade ID", "Seller Trade ID", "Stock name", "Quantity", "Price(1 st)", "Total","Time"
    //        );
    //        System.out.println("+----------+----------+----------+----------+---------------+---------------+----------+----------+---------------+");
    //        for(trading.Transaction transaction : transactions){
    //            transaction.toString();
    //        }
    //        System.out.println("+----------+----------+----------+----------+---------------+---------------+----------+----------+---------------+");
    //        System.out.println();
    //    }

    //    public void displayOrders(){
    //        if (orderIds.isEmpty()) {
    //            System.out.println("No orders placed from this account.");
    //            return;
    //        }
    //        System.out.println("\nORDERS FROM THIS ACCOUNT");
    //        System.out.println("trading.Order IDs: " + orderIds);
    //        System.out.println("Total Orders: " + orderIds.size());
    //        System.out.println();
    //    }

    //    private void deactiveAccount(){
    //        this.isActive = false;
    //        System.out.println("Account ID : " + this.tradingAccountId +", deactivated Successfully");
    //    }
}
