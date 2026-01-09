package dbOperations;

import java.util.*;

public class Condition {

    private ArrayList<String> columns = new ArrayList<>();
    private ArrayList<Object> values = new ArrayList<>();
    private ArrayList<Boolean> isNullCols = new ArrayList<>();

    public Condition add(String column, Object value) {
        columns.add(column);
        values.add(value);
        isNullCols.add(false);
        return this;
    }

    // add null cond -> SET column = NULL
    public Condition addNull(String column) {
        columns.add(column);
        values.add(null);
        isNullCols.add(true);
        return this;
    }

    public String toSQL() {
        if (columns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            if (isNullCols.get(i)) {
                sb.append(columns.get(i)).append(" = NULL");
            } else {
                sb.append(columns.get(i)).append(" = ?");
            }
        }
        return sb.toString();
    }

    // for UPDATE
    public String toSetSQL() {
        if (columns.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (isNullCols.get(i)) {
                sb.append(columns.get(i)).append(" = NULL");
            } else {
                sb.append(columns.get(i)).append(" = ?");
            }
        }
        return sb.toString();
    }

    // values list (not NULL values)
    public ArrayList<Object> getValues() {
        ArrayList<Object> nonNullValues = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (!isNullCols.get(i)) {
                nonNullValues.add(values.get(i));
            }
        }
        return nonNullValues;
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }
}
