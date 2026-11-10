package service;

import java.util.*;
import config.*;

class Parser {
    public static Condition parseSingleCondition(String cond) {
        for (String sign : new String[]{">=", "<=", "=", ">", "<"}) {
            int i = cond.indexOf(sign);
            if (i > 0) {
                Operator op = switch (sign) {
                    case "="  -> Operator.EQ;
                    case ">=" -> Operator.GE;
                    case "<=" -> Operator.LE;
                    case ">"  -> Operator.G;
                    default   -> Operator.L;
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

public class Service {
//    private Repository repository;

//    public GoodsService(Repository Repository) {
//        this.repository = repository;
//    }

    public Service() {

    }

    public void select(String tableName, String cols, String condition) {
        List<String> colsList = Arrays.stream(cols.split(",")).toList();
        List<List<Condition>> conditionList = Parser.parseComplexCondition(condition);
//        repository.select(tableName, conditionList); - вернет мне все строки, подходящие под condition
//        надо оставить в них те колонки, которые есть в colsList и вернуть в консольный интерфейс
    }

    public void insert(String tableName, String cols, String values) throws IllegalArgumentException {
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
        // вернуть сообщение об успешном удалении, если repo.insert не выбросило исключений
    }

    public void update(String tableName, String set, String where) {
        Map<String, Object> entries = Parser.parseSet(set);
//        return repository.update(tableName, entries);
        // вернуть сообщение об успешном обновлении, если repo.update не выбросило исключений
    }
}
