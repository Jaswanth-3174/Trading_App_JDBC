package dbConnection;

import java.sql.*;
public class IndexUsageCheck {
    public static void check() throws SQLException {
        String sql = """
                EXPLAIN ANALYZE
                SELECT *
                FROM orders
                WHERE stock_id = ? AND is_buy = ?
                ORDER BY price ASC, order_id ASC
                LIMIT 10
                """;
        Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, 1);
        ps.setInt(2, 0);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }
}
