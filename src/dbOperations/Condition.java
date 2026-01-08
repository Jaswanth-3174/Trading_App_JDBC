package dbOperations;

import java.util.*;

public class Condition {

    private ArrayList<String> columns = new ArrayList<>();
    private ArrayList<Object> values = new ArrayList<>();

    public Condition add(String column, Object value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    public String toSQL() {  // building query
        if (columns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            sb.append(columns.get(i)).append(" = ?");
        }
        return sb.toString();
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }
}
