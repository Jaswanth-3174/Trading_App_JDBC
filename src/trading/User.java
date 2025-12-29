package trading;

import account.DematAccount;
import account.TradingAccount;
import util.*;

public class User {
    private static int idCounter = 1;
    private int userId;
    private String userName;
    private String password;
    private String panNumber;
    private TradingAccount tradingAccount;
    // private DematAccount dematAccount;
    private int dematId;
    private boolean isPromoter;
    private boolean isActive;

    public User(){}

    public User(String userName, String password, String panNumber, boolean isPromoter) {
        this.userId = idCounter++;
        this.userName = userName;
        this.password = password;
        this.panNumber = panNumber;
        this.tradingAccount = null;
        this.isPromoter = isPromoter;
        this.isActive = true;
    }

    public static int getIdCounter() {
        return idCounter;
    }

    public boolean login(InputHandler inputHandler) {
        String pass = inputHandler.getString("Enter password: ");
        return this.password.equals(pass);
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int id){
        this.userId = id;
    }

    public int getDematId(){
        return this.dematId;
    }

    public void setDematId(int id){
        this.dematId = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String name){
        this.userName = name;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public void setPanNumber(String panNumber){
        this.panNumber = panNumber;
    }

    public void setPromoter(boolean b){
        this.isPromoter = b;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getPanNumber() {
        return this.panNumber;
    }

    public void setTradingAccount(TradingAccount tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public TradingAccount getTradingAccount() {
        return this.tradingAccount;
    }

    public boolean isPromoter() {
        return this.isPromoter;
    }

    public boolean isActive() {
        return this.isActive;
    }
}
