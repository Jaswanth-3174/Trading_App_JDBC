package dao;

import dbOperations.*;
import account.TradingAccount;

import java.sql.SQLException;
import java.util.*;

public class TradingAccountDAO {

    private static String tableName = "trading_accounts";

    public TradingAccount findByUserId(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("user_id", userId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToTradingAccount(rows.get(0)) : null;
    }

    public TradingAccount createTradingAccount(int userId, double balance) throws SQLException {
        Condition data = new Condition();
        data.add("user_id", userId);
        data.add("balance", balance);
        data.add("reserved_balance", 0.0);
        int tradingId = InsertOperation.insert(tableName, data);
        return tradingId > 0 ? findByUserId(userId) : null;
    }

    public boolean reserveBalance(int userId, double amount) throws SQLException {
        TradingAccount acc = findByUserId(userId);
        if (acc == null || acc.getBalance() < amount) return false;

        Condition set = new Condition();
        set.add("balance", acc.getBalance() - amount);
        set.add("reserved_balance", acc.getReservedBalance() + amount);
        Condition where = new Condition();
        where.add("user_id", userId);
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean releaseReservedBalance(int userId, double amount) throws SQLException {
        TradingAccount acc = findByUserId(userId);
        if (acc == null || acc.getReservedBalance() < amount) return false;

        Condition set = new Condition();
        set.add("balance", acc.getBalance() + amount);
        set.add("reserved_balance", acc.getReservedBalance() - amount);
        Condition where = new Condition();
        where.add("user_id", userId);
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean debit(int userId, double amount) throws SQLException {
        TradingAccount acc = findByUserId(userId);
        if (acc == null || acc.getReservedBalance() < amount) return false;

        Condition set = new Condition();
        set.add("reserved_balance", acc.getReservedBalance() - amount);
        Condition where = new Condition();
        where.add("user_id", userId);
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public boolean credit(int userId, double amount) throws SQLException {
        TradingAccount acc = findByUserId(userId);
        if (acc == null) return false;

        Condition set = new Condition();
        set.add("balance", acc.getBalance() + amount);
        Condition where = new Condition();
        where.add("user_id", userId);
        return UpdateOperation.update(tableName, set, where) > 0;
    }

    public double getAvailableBalance(int userId) throws SQLException {
        TradingAccount acc = findByUserId(userId);
        return acc != null ? acc.getBalance() : 0.0;
    }

    private TradingAccount mapToTradingAccount(HashMap<String, Object> row) {
        TradingAccount account = new TradingAccount();
        account.setTradingAccountId(((Number) row.get("trading_id")).intValue());
        account.setUserId(((Number) row.get("user_id")).intValue());
        account.setBalance(((Number) row.get("balance")).doubleValue());
        account.setReservedBalance(((Number) row.get("reserved_balance")).doubleValue());
        return account;
    }
}