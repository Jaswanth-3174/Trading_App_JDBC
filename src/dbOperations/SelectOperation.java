package dbOperations;

import java.sql.*;
import java.util.*;

public class SelectOperation {

    public static ArrayList<HashMap<String, Object>> select(String tableName, Condition condition) throws SQLException {
        return selectWithJoin(tableName, null, null, condition, null);
    }

    public static ArrayList<HashMap<String, Object>> select(String tableName, String[] columns, Condition condition) throws SQLException{
        return selectWithJoin(tableName, columns, null, condition, null);
    }

    public static ArrayList<HashMap<String, Object>> selectWithJoin(String tableName, String[] columns, String join, Condition condition, String order) throws SQLException{
        ArrayList<HashMap<String, Object>> rows = new ArrayList<>();

        String columnList;
        if(columns == null || columns.length == 0){
            columnList = "*";
        }else{
            columnList = String.join(", ", columns);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(columnList).append(" FROM ").append(tableName);

        if(join != null && !join.isEmpty()){ // join
            sql.append(" ").append(join);
        }

        // Add WHERE clause if provided
        if (condition != null && !condition.isEmpty()) {
            sql.append(" WHERE ").append(condition.toSQL());
        }

        if(order != null && !order.isEmpty()){
            sql.append(" ORDER BY ").append(order);
        }

        System.out.println("DEBUG SQL: " + sql.toString());

        // execute
        Connection con = DbHelper.getConnection();
        PreparedStatement ps = con.prepareStatement(sql.toString());
        if (condition != null) {
            ArrayList<Object> values = condition.getValues();
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
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
