package trading;

public class User {
    private int userId;
    private String userName;
    private String password;
    private int dematId;
    private boolean isPromoter;
    private boolean isActive;

    public User(){}

    public User(String userName, String password, String panNumber, boolean isPromoter) {
        this.userName = userName;
        this.password = password;
        this.isPromoter = isPromoter;
        this.isActive = true;
    }

    public int getUserId() {
        return this.userId;
    }
    public void setUserId(int id){
        this.userId = id;
    }

    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String name){
        this.userName = name;
    }

    public int getDematId(){
        return this.dematId;
    }
    public void setDematId(int id){
        this.dematId = id;
    }

    public String getPassword(){return this.password;}
    public void setPassword(String password){
        this.password = password;
    }

    public boolean isPromoter(){return this.isPromoter;}
    public void setPromoter(boolean b){
        this.isPromoter = b;
    }

    public boolean isActive() {
        return this.isActive;
    }
    public void setActive(boolean active) {
        this.isActive = active;
    }

}
