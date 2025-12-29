package model;

public class DematAccount {
    private int dematId;
    private String panNumber;
    private String password;
    
    public DematAccount() {}
    
    public DematAccount(int dematId, String panNumber, String password) {
        this.dematId = dematId;
        this.panNumber = panNumber;
        this.password = password;
    }
    
    // Getters and Setters
    public int getDematId() { return dematId; }
    public void setDematId(int dematId) { this.dematId = dematId; }
    
    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}