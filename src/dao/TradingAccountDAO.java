package dao;

import dbConnection.DatabaseConfig;
import account.TradingAccount;
import java.sql.*;

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

    public TradingAccount createTradingAccount(int userId, double balance){
        String query = "INSERT INTO trading_accounts(user_id, balance) values (?, ?)";
        try (
                Connection con = DatabaseConfig.getConnection();
                PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                ){
            ps.setInt(1, userId);
            ps.setDouble(2, balance);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next()){
                TradingAccount tradingAccount = new TradingAccount();
                tradingAccount.setTradingAccountId(rs.getInt(1));
                tradingAccount.setUserId(userId);
                tradingAccount.setBalance(balance);
                tradingAccount.setReservedBalance(0);
                return findByUserId(userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean reserveBalance(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET reserved_balance = reserved_balance + ?, balance = balance - ? WHERE user_id = ? AND balance >= ? ";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setDouble(2, amount);
            ps.setInt(3, userId);
            ps.setDouble(4, amount);
            return ps.executeUpdate() > 0; // int rows = ps.executeUpdate(); true -> 1
        }
    }

    public boolean releaseReservedBalance(int userId, double amount) throws SQLException{
        String sql = "UPDATE trading_accounts SET reserved_balance = reserved_balance - ?, balance = balance + ? WHERE user_id = ? AND reserved_balance >= ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setDouble(2, amount);
            ps.setInt(3, userId);
            ps.setDouble(4, amount);
            return ps.executeUpdate() > 0; // int rows = ps.executeUpdate(); true -> 1
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean debit(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET reserved_balance = reserved_balance - ? WHERE user_id = ? AND reserved_balance >= ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.setDouble(3, amount);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean credit(int userId, double amount) throws SQLException {
        String sql = "UPDATE trading_accounts SET balance = balance + ? WHERE user_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public double getAvailableBalance(int userId){
        TradingAccount tradingAccount = findByUserId(userId);
        return tradingAccount != null ? tradingAccount.getBalance() : 0.0;
    }

    private TradingAccount printRow(ResultSet rs) throws SQLException {
        TradingAccount tradingAccount = new TradingAccount();
        tradingAccount.setTradingAccountId(rs.getInt(1));
        tradingAccount.setUserId(rs.getInt(2));
        tradingAccount.setBalance(rs.getDouble(3));
        tradingAccount.setReservedBalance(rs.getDouble(4));
        return tradingAccount;
    }
}
