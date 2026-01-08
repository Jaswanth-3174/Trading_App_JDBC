package dbOperations;

import java.sql.*;

public class UpdateOperation {

    public static int update(String table, Condition setData, Condition whereCondition) throws SQLException {

        if (setData == null || setData.isEmpty()) {
            return 0;
        }

        String tableName = table.split(" ")[0];
        String setSQL = setData.toSQL().replace(" AND ", ", ");

        String sql;
        if (whereCondition == null || whereCondition.isEmpty()) {
            sql = "UPDATE " + tableName + " SET " + setSQL;
        } else {
            sql = "UPDATE " + tableName + " SET " + setSQL + " WHERE " + whereCondition.toSQL();
        }

        Connection con = DbHelper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        int i = 1;
        for (Object val : setData.getValues()) {
            ps.setObject(i++, val);
        }
        if (whereCondition != null) {
            for (Object val : whereCondition.getValues()) {
                ps.setObject(i++, val);
            }
        }
        return ps.executeUpdate();
    }
}