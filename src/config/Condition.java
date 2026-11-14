package config;

import java.util.List;
import java.util.Map;

public class Condition {
    private final String col;
    private final Operator op;
    private final Object value;

    public Condition(String col, Operator op, Object value) {
        this.col = col;
        this.op = op;
        this.value = value;
    }

    public Operator getOp() {
        return op;
    }

    public Object getValue() {
        return value;
    }

    public String getCol() {
        return col;
    }

    private boolean result(Object actual) {
        if (actual == null) return false;

        if (op == Operator.EQS)
            return actual instanceof String a && value instanceof String b && a.equals(b);

        int c;
        if (actual instanceof Number a && value instanceof Number b)
            c = Double.compare(a.doubleValue(), b.doubleValue());
        else if (actual instanceof Boolean a && value instanceof Boolean b)
            c = Boolean.compare(a, b);
        else if (actual instanceof String a && value instanceof String b)
            c = a.compareTo(b);
        else
            throw new IllegalArgumentException("Нельзя сравнить столбец " + col + " (" +
                    actual.getClass().getSimpleName() + ") со значением " + value);

        return switch (op) {
            case EQ -> c == 0;
            case G  -> c > 0;
            case L  -> c < 0;
            case GE -> c >= 0;
            case LE -> c <= 0;
            case EQS -> false;
        };
    }

    public static boolean matches(Map<String, Object> row, List<List<Condition>> where) {
        if (where.isEmpty()) return true;

        for (List<Condition> group : where) {
            boolean groupMatch = true;
            for (Condition cond : group) {
                if (!cond.result(row.get(cond.col))) {
                    groupMatch = false;
                    break;
                }
            }
            if (groupMatch) return true;
        }
        return false;
    }
}
