package dao;


import dbConnection.DatabaseConfig;
import trading.User;
import java.sql.*;
import java.util.*;

public class UserDAO {

    public User findById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return printRow(rs);
            }
        }
        return null;
    }

    public static String findUsernameById(int userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE user_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }

    public User findByDematId(int dematId) throws SQLException {
        String sql = "SELECT * FROM users WHERE demat_id = ? AND isActive = TRUE";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dematId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return printRow(rs);
            }
        }
        return null;
    }

    public User createUser(String userName, String password, int dematId, boolean isPromoter) throws SQLException {
        String sql = "INSERT INTO users (username, password, demat_id, isPromoter) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, userName);
            ps.setString(2, password);
            ps.setInt(3, dematId);
            ps.setBoolean(4, isPromoter);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return findById(keys.getInt(1));
            }
        }
        return null;
    }

    public boolean authenticateUser(int userId, String password) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE user_id = ? AND password = ? AND isActive = TRUE";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, password);
            return ps.executeQuery().next();
        }
    }

    public boolean isActiveUserLinkedWithDematId(int dematId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE demat_id = ? AND isActive = TRUE";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dematId);
            return ps.executeQuery().next();
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = "UPDATE users SET isActive = FALSE WHERE user_id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<User> listAllActiveUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE isActive = TRUE";
        try (Connection con = DatabaseConfig.getConnection();
             Statement ps = con.createStatement();
             ResultSet rs = ps.executeQuery(sql)) {
            while (rs.next()) {
                users.add(printRow(rs));
            }
        }
        return users;
    }

    private User printRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUserName(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setDematId(rs.getInt("demat_id"));
        user.setPromoter(rs.getBoolean("isPromoter"));
        user.setActive(rs.getBoolean("isActive"));
        return user;
    }
}