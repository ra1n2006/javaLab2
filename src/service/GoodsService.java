package service;

import repository.GoodsRepository;

import java.util.*;

enum Opertator {
    EQ,
    EQS,
    GE,
    LE,
    G,
    L
}

class Condition {
    private String col;
    private Opertator op;
    private Object value;

    Condition(String col, Opertator op, Object value) {
        this.col = col;
        this.op = op;
        this.value = value;
    }

    private boolean result(Object actual) {
        if (actual == null) return false;

        if (op == Opertator.EQS)                       // точное равенство строк
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

    public boolean matches(Map<String, Object> row, List<List<Condition>> where) {
        if (where.isEmpty()) return true;

        return where.stream().anyMatch(group ->
                group.stream().allMatch(cond ->
                        cond.result(row.get(cond.col))));
    }
}

class Parser {
    public static Condition parseSingleCondition(String cond) {
        for (String sign : new String[]{">=", "<=", "=", ">", "<"}) {
            int i = cond.indexOf(sign);
            if (i > 0) {
                Opertator op = switch (sign) {
                    case "="  -> Opertator.EQ;
                    case ">=" -> Opertator.GE;
                    case "<=" -> Opertator.LE;
                    case ">"  -> Opertator.G;
                    default   -> Opertator.L;
                };
                return new Condition(cond.substring(0, i).trim(), op, cond.substring(i + sign.length()).trim());
            }
        }
        throw new IllegalArgumentException("Неверное условие: " + cond);
    }

    public static List<List<Condition>> parseComplexCondition(String where) {

        return Arrays.stream(where.split("(?i)\\s+OR\\s+"))
                .map(orPart -> Arrays.stream(orPart.split("(?i)\\s+AND\\s+"))
                        .map(Parser::parseSingleCondition)
                        .toList())
                .toList();
    }

    public static Object parseValue(String row) {
        row = row.trim();
        if (row.startsWith("'") && row.endsWith("'") && row.length() >= 2)
            return row.substring(1, row.length() - 1);
        if (row.contains("."))
            return Double.parseDouble(row);
        return Integer.parseInt(row);
    }

    public static Map<String, Object> parseSet(String set) {
        Map<String, Object> table = new HashMap<>();
        for (String part : set.split(",")) {
            int i = part.indexOf('=');
            if (i < 0) throw new IllegalArgumentException("Неверное присваивание: " + part.trim());
            table.put(part.substring(0, i).trim(), parseValue(part.substring(i + 1)));
        }
        return table;
    }
}

public class GoodsService {
//    private Repository repository;

//    public GoodsService(Repository Repository) {
//        this.repository = repository;
//    }

    public GoodsService() {

    }

    public void select(String tableName, String cols, String condition) {
        List<String> colsList = Arrays.stream(cols.split(",")).toList();
        List<List<Condition>> conditionList = Parser.parseComplexCondition(condition);
//        return repository.select(tableName, conditionList);
    }

    public void insert(String tableName, String cols, String values) {
        List<String> colsNames = new ArrayList<>();
        if (cols == null) {
//            colsNames = repository.getColumns(tableName);
        } else {
            colsNames = Arrays.stream(cols.split(",")).map(String::trim).toList();
        }

        String[] vals = values.trim().substring(1, values.trim().length() - 1).split(",");
        if (vals.length != colsNames.size())
            throw new IllegalArgumentException("Столбцов " + colsNames.size() + ", значений " + vals.length);

        Map<String, Object> rows = new LinkedHashMap<>();
        for (int i = 0; i < vals.length; i++) rows.put(colsNames.get(i), Parser.parseValue(vals[i]));
//        return repository.insert(tableName, rows);
    }

    public void delete(String tableName, String condition){
        List<List<Condition>> conditionList = Parser.parseComplexCondition(condition);
//        return repository.insert(tableName, conditionList);
    }

    public void update(String tableName, String set, String where) {
        Map<String, Object> entries = Parser.parseSet(set);
//        return repository.update(tableName, entries);
    }
}
