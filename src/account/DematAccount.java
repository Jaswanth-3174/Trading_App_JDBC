package account;

public class DematAccount {
    private int dematAccountId;
    private String panNumber;
    private String password;

    public DematAccount() {}

    public DematAccount(String panNumber, String password) {
        this.panNumber = panNumber;
        this.password = password;
    }

    public int getDematAccountId() { return dematAccountId; }
    public void setDematAccountId(int dematAccountId) { this.dematAccountId = dematAccountId; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}