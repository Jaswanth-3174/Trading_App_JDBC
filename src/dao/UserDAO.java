package dao;

import dbOperations.*;
import trading.User;

import java.sql.SQLException;
import java.util.*;

public class UserDAO {

    private static String tableName = "users";

    public User findById(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("user_id", userId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToUser(rows.get(0)) : null;
    }

    public static String findUsernameById(int userId) throws SQLException {
        Condition c = new Condition();
        c.add("user_id", userId);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"username"}, c);
        return !rows.isEmpty() ? (String) rows.get(0).get("username") : null;
    }

    public User findByDematId(int dematId) throws SQLException {
        Condition c = new Condition();
        c.add("demat_id", dematId);
        c.add("isActive", true);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        return !rows.isEmpty() ? mapToUser(rows.get(0)) : null;
    }

    public User createUser(String userName, String password, int dematId, boolean isPromoter) throws SQLException {
        Condition data = new Condition();
        data.add("username", userName);
        data.add("password", password);
        data.add("demat_id", dematId);
        data.add("isPromoter", isPromoter);
        int userId = InsertOperation.insert(tableName, data);
        return userId > 0 ? findById(userId) : null;
    }

    public boolean authenticateUser(int userId, String password) throws SQLException {
        Condition c = new Condition();
        c.add("user_id", userId);
        c.add("password", password);
        c.add("isActive", true);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"user_id"}, c);
        return !rows.isEmpty();
    }

    public boolean isActiveUserLinkedWithDematId(int dematId) throws SQLException {
        Condition c = new Condition();
        c.add("demat_id", dematId);
        c.add("isActive", true);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, new String[]{"user_id"}, c);
        return !rows.isEmpty();
    }

    public boolean deleteUser(int userId) throws SQLException {
        Condition set = new Condition();
        set.add("isActive", false);
        Condition where = new Condition();
        where.add("user_id", userId);
        int affected = UpdateOperation.update(tableName, set, where);
        return affected > 0;
    }

    public List<User> listAllActiveUsers() throws SQLException {
        Condition c = new Condition();
        c.add("isActive", true);
        ArrayList<HashMap<String, Object>> rows = SelectOperation.select(tableName, c);
        List<User> users = new ArrayList<>();
        for (HashMap<String, Object> row : rows) {
            users.add(mapToUser(row));
        }
        return users;
    }

    private User mapToUser(HashMap<String, Object> row) {
        User user = new User();
        user.setUserId(((Number) row.get("user_id")).intValue());
        user.setUserName((String) row.get("username"));
        user.setPassword((String) row.get("password"));
        user.setDematId(((Number) row.get("demat_id")).intValue());
        user.setPromoter((Boolean) row.get("isPromoter"));
        user.setActive((Boolean) row.get("isActive"));
        return user;
    }
}