package dbOperations;

import java.sql.*;
import java.util.*;

public class SelectOperation {

    // basic select
    public static ArrayList<HashMap<String, Object>>
    select(String tableName, Condition condition) throws SQLException {
        return selectWithAdvancedCondition(tableName, null,
                null, condition, null, null, 0);
    }

    public static ArrayList<HashMap<String, Object>>
    select(String tableName, String[] columns, Condition condition) throws SQLException {
        return selectWithAdvancedCondition(tableName, columns, null, condition,
                null, null, 0);
    }

    // select with joins
    public static ArrayList<HashMap<String, Object>>
    selectWithJoin(String tableName, String[] columns, String join,
                   Condition condition, String order) throws SQLException {
        return selectWithAdvancedCondition(tableName, columns, join, condition, null, order, 0);
    }

    public static ArrayList<HashMap<String, Object>>
    selectWithJoin(String tableName, String[] columns, String join, Condition condition,
                   String order, int limit) throws SQLException {
        return selectWithAdvancedCondition(tableName, columns, join, condition, null,
                order, limit);
    }

    public static ArrayList<HashMap<String, Object>>
    selectWithAdvancedCondition(String table,
            String[] columns, String join, Condition base, SpecialCondition extra,
            String orderBy, int limit) throws SQLException {

        ArrayList<HashMap<String, Object>> rows = new ArrayList<>();
        String columnList = (columns == null || columns.length == 0)
                ? "*" : String.join(", ", columns);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(columnList).append(" FROM ").append(table);

        if (join != null && !join.isEmpty()) {
            sql.append(" ").append(join);
        }

        boolean hasWhere = false;

        if (base != null && !base.isEmpty()) {
            sql.append(" WHERE ").append(base.toSQL());
            hasWhere = true;
        }

        if (extra != null && !extra.isEmpty()) {
            sql.append(hasWhere ? " AND " : " WHERE ").append("(").append(extra.toSQL()).append(")");
        }

        if (orderBy != null && !orderBy.isEmpty()) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }

        // System.out.println("SELECT DEBUG => " + sql);  // debug

        Connection con = DbHelper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql.toString());

        int idx = 1;
        if (base != null) {
            for (Object v : base.getValues()) {
                ps.setObject(idx++, v);
            }
        }

        if (extra != null) {
            for (Object v : extra.getValues()) {
                ps.setObject(idx++, v);
            }
        }

        ResultSet rs = ps.executeQuery();
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();
        while (rs.next()) {
            HashMap<String, Object> row = new HashMap<>();
            for (int i = 1; i <= colCount; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }
        return rows;
    }
}
