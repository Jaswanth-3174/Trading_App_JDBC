package dbOperations;

import java.sql.*;
import java.util.*;

public class InsertOperation {

    public static int insert(String table, Condition data) throws SQLException {
        String tableName = table.split(" ")[0];
        if (data == null || data.isEmpty()) {
            return -1;
        }

        ArrayList<Object> values = data.getValues();
        String condition = data.toSQL();
        String[] parts = condition.split(" AND ");
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            String colName = parts[i].split(" = ")[0].trim();
            if (i > 0) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(colName);
            placeholders.append("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
       // System.out.println("DEBUG INSERT SQL: " + sql);

        Connection con = DbHelper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int i = 0; i < values.size(); i++) {
            ps.setObject(i + 1, values.get(i));
        }
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }
}

