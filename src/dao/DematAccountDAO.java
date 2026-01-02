package dao;

import dbConnection.DatabaseConfig;
import account.DematAccount;

import java.sql.*;

public class DematAccountDAO {
    public DematAccount findById(int id){
        String query = "SELECT * from demat_accounts where demat_id = ?";
        try(
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query);
                ){
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return printRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DematAccount findByPanNumber(String panNumber){
        String query = "SELECT * FROM demat_accounts where pan_number = ?";
        try(
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query);
                ){
            ps.setString(1, panNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return printRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public DematAccount createDematAccount(String panNumber, String password){
        String query = "INSERT INTO demat_accounts (pan_number, password) VALUES (?, ?)";
        try(
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ){
            ps.setString(1, panNumber);
            ps.setString(2, password);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys(); // gets demat account auto incremented
            if(rs.next()){
                int dematId = rs.getInt(1);
                DematAccount dematAccount = new DematAccount();
                dematAccount.setDematAccountId(dematId);
                dematAccount.setPanNumber(panNumber);
                dematAccount.setPassword(password);
                System.out.println("Created demat account for pan number : " + panNumber);
                return findById(dematId);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean authenticate(String panNumber, String password){
        String query = "SELECT 1 FROM demat_accounts WHERE pan_number = ? AND password = ?";
        try(
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query);
                ){
            ps.setString(1, panNumber);
            ps.setString(2, password);
            return  ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private DematAccount printRow(ResultSet rs) throws SQLException {
        DematAccount dematAccount = new DematAccount();
        dematAccount.setDematAccountId(rs.getInt(1));
        dematAccount.setPanNumber(rs.getString(2));
        dematAccount.setPassword(rs.getString(3));
        return dematAccount;
    }
}
