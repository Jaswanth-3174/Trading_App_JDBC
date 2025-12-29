package account;

import trading.*;
import util.*;

public class DematAccount {
    private static int idCounter = 1;
    private int demandAccountId;
    private String panNumber;
    private String password;
    private StockPortfolio portfolio;

    public DematAccount(){

    }

    public DematAccount(String panNumber, String password){
        this.demandAccountId = idCounter++;
        this.panNumber = panNumber;
        this.password = password;
        this.portfolio = new StockPortfolio();
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public int getDemandAccountId(){
        return this.demandAccountId;
    }

    public void setDemandAccountId(int id){
        this.demandAccountId = id;
    }

    public void setPanNumber(String panNumber){
        this.panNumber = panNumber;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPanNumber(){
        return this.panNumber;
    }

    public boolean authenticateWithPrompt(InputHandler inputHandler, String prompt) {
        String pass = inputHandler.getString(prompt);
        return this.password.equals(pass);
    }

    boolean authenticate(String pass) {
        return this.password.equals(pass);
    }

    public StockPortfolio getPortfolio() {
        return this.portfolio;
    }

    public void addShares(String stockName, int quantity){
        portfolio.addShares(stockName, quantity);
    }

    public boolean sellShares(String stockName, int quantity){
        return portfolio.sellShares(stockName, quantity);
    }

    public boolean reserveStocks(String stockName, int quantity){
        return portfolio.reserveStocks(stockName, quantity);
    }

    public void releaseReservedStocks(String stockName, int quantity){
        portfolio.releaseReservedStocks(stockName, quantity);
    }

    public void getHoldings(){
        portfolio.displayHoldings();
    }

    public int getAvailableQuantity(String stockName){
        return portfolio.getAvailableQuantity(stockName);
    }

    public boolean hasHoldings() {
        return portfolio.hasHoldings();
    }
}
