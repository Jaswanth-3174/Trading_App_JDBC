package dao;

import dbOperations.*;
import account.DematAccount;

import java.sql.SQLException;
import java.util.*;

public class DematAccountDAO {

    private static String tableName = "demat_accounts";

    public DematAccount findById(int dematId) throws SQLException {
        Condition c = new Condition();
        c.add("demat_id", dematId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToDematAccount(rows.get(0)) : null;
    }

    public DematAccount findByPanNumber(String panNumber) throws SQLException {
        Condition c = new Condition();
        c.add("pan_number", panNumber);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToDematAccount(rows.get(0)) : null;
    }

    public DematAccount createDematAccount(String panNumber, String password) throws SQLException {
        Condition data = new Condition();
        data.add("pan_number", panNumber);
        data.add("password", password);
        int dematId = InsertOperation.insert(tableName, data);
        return dematId > 0 ? findById(dematId) : null;
    }

    public boolean updatePassword(int dematId, String password) throws SQLException {
        Condition set = new Condition();
        set.add("password", password);
        Condition where = new Condition();
        where.add("demat_id", dematId);
        int affected = UpdateOperation.update(tableName, set, where);
        return affected > 0;
    }

    public boolean deleteAccount(int dematId) throws SQLException {
        Condition c = new Condition();
        c.add("demat_id", dematId);
        int affected = DeleteOperation.delete(tableName, c);
        return affected > 0;
    }

    public List<DematAccount> listAll() throws SQLException {
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, null);
        List<DematAccount> accounts = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            accounts.add(mapToDematAccount(row));
        }
        return accounts;
    }

    public boolean authenticate(String panNumber, String password) throws SQLException {
        Condition c = new Condition();
        c.add("pan_number", panNumber);
        c.add("password", password);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"demat_id"}, c);
        return !rows.isEmpty();
    }

    private DematAccount mapToDematAccount(HashMap<String, Object> row) {
        DematAccount account = new DematAccount();
        account.setDematAccountId(((Number) row.get("demat_id")).intValue());
        account.setPanNumber((String) row.get("pan_number"));
        account.setPassword((String) row.get("password"));
        return account;
    }
}