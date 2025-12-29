package DAO;

import DbConnection.DatabaseConfig;
import account.TradingAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TradingAccountDAO {
    public TradingAccount findByUserId(int userId){
        String query = "SELECT * FROM trading_accounts WHERE user_id = ?";
        try(
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query);
                ){
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return printRow(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public TradingAccount createTradingAccount(int userId){
        double balance = Math.random() * 4000 + 1000; // 1000 - 5000
        return null;
    }

    private TradingAccount printRow(ResultSet rs) throws SQLException {
        TradingAccount tradingAccount = new TradingAccount();
        tradingAccount.setTradingAccountId(rs.getInt(1));
        tradingAccount.setUserId(rs.getInt(2));
        tradingAccount.setBalance(rs.getInt(3));
        tradingAccount.setReservedBalance(rs.getInt(4));
        return tradingAccount;
    }
}
