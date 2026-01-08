package dbOperations;

import java.sql.*;
import java.util.*;

public class DeleteOperation {
    public static int delete(String tableName, Condition condition) throws SQLException{

        String sql;
        if(condition == null || condition.isEmpty()){
            sql = "DELETE FROM " + tableName;
        }else{
            sql = "DELETE FROM " + tableName + " WHERE " + condition.toSQL();
        }

        Connection conn = DbHelper.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        if(condition != null){
            ArrayList<Object> values = new ArrayList<>();
            for(int i=0; i<values.size(); i++){
                ps.setObject(i+1, values.get(i));
            }
        }
        return ps.executeUpdate();
    }
}
