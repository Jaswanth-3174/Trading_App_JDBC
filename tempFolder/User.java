package model;

public class User {
    private int userId;
    private String username;
    private String password;
    private int dematId;
    private boolean isPromoter;
    private boolean isActive;
    
    public User() {}
    
    public User(String username, String password, int dematId, boolean isPromoter) {
        this.username = username;
        this.password = password;
        this.dematId = dematId;
        this.isPromoter = isPromoter;
        this.isActive = true;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getDematId() { return dematId; }
    public void setDematId(int dematId) { this.dematId = dematId; }
    
    public boolean isPromoter() { return isPromoter; }
    public void setPromoter(boolean promoter) { isPromoter = promoter; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}