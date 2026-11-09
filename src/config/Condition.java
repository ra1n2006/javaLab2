package config;

import config.Operator;

import java.util.List;
import java.util.Map;

public class Condition {
    private String col;
    private Operator op;
    private Object value;

    public Condition(String col, Operator op, Object value) {
        this.col = col;
        this.op = op;
        this.value = value;
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

        return where.stream().anyMatch(group ->
                group.stream().allMatch(cond ->
                        cond.result(row.get(cond.col))));
    }
}
