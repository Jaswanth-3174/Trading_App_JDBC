package dbOperations;

import java.sql.*;
import java.util.*;

public class DeleteOperation {
    public static int delete(String table, Condition condition) throws SQLException {

        String tableName = table.split(" ")[0];

        String sql;
        if (condition == null || condition.isEmpty()) {
            sql = "DELETE FROM " + tableName;
        } else {
            sql = "DELETE FROM " + tableName + " WHERE " + condition.toSQL();
        }

        System.out.println("DEBUG DELETE SQL: " + sql);

        // Execute
        Connection con = DbHelper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        if (condition != null && !condition.isEmpty()) {
            ArrayList<Object> values = condition.getValues();
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
        }
        return ps.executeUpdate();
    }
}
