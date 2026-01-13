package dbOperations;

import java.util.*;

public class SpecialCondition {

    private final String sql;
    private final List<Object> values;

    public SpecialCondition(String sql, Object... values) {
        this.sql = sql;
        this.values = Arrays.asList(values);
    }

    public String toSQL() {
        return sql;
    }

    public List<Object> getValues() {
        return values;
    }

    public boolean isEmpty() {
        return sql == null || sql.isEmpty();
    }
}
