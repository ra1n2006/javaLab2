package service;

import java.io.IOException;
import java.util.*;
import config.*;
import model.Shop;
import model.Table;
import repository.Repository;

class Parser {
    public static Condition parseSingleCondition(String cond) {
        for (String sign : new String[]{">=", "<=", "=", ">", "<"}) {
            int i = cond.indexOf(sign);
            if (i > 0) {
                String raw = cond.substring(i + sign.length()).trim();
                Operator op = switch (sign) {
                    case "="  -> raw.startsWith("'") ? Operator.EQS : Operator.EQ;
                    case ">=" -> Operator.GE;
                    case "<=" -> Operator.LE;
                    case ">"  -> Operator.G;
                    default   -> Operator.L;
                };
                return new Condition(cond.substring(0, i).trim(), op, parseValue(raw));
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
    private Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
    }

    public void select(String tableName, String cols, String condition) {
        List<String> colsList = new ArrayList<>();
        List<List<Condition>> conditionList = new ArrayList<>();
//        Table t = repository.load(tableName);
//        if (cols.trim().equals("*")) {
//            colsList = t.columnNames();
//        } else {
//            colsList = Arrays.stream(cols.split(",")).toList();
//        }
//        for (Map<String, Object> row: t.getRows()) {
//
//        }
        if (condition != null) {
            conditionList = Parser.parseComplexCondition(condition);
        } else {
            conditionList = new ArrayList<>();
        }
        try {
            List<?> rows = repository.select(tableName, conditionList);
            for (Object row : rows) {
                // представить объект в виде словаря и вывести только нужные колонки
                System.out.println(row.toString());
            }
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        надо оставить в них те колонки, которые есть в colsList и вернуть в консольный интерфейс
    }

    public void insert(String tableName, String cols, String values) throws IllegalArgumentException {
        List<String> colsNames = new ArrayList<>();
        if (cols == null) {
            colsNames = repository.getColumns(tableName);
        } else {
            colsNames = Arrays.stream(cols.split(",")).map(String::trim).toList();
        }

        String[] vals = values.trim().substring(1, values.trim().length() - 1).split(",");
        if (vals.length != colsNames.size())
            throw new IllegalArgumentException("Столбцов " + colsNames.size() + ", значений " + vals.length);

        Map<String, Object> rows = new LinkedHashMap<>();
        for (int i = 0; i < vals.length; i++) rows.put(colsNames.get(i), Parser.parseValue(vals[i]));
        try {
            repository.insert(tableName, rows);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void delete(String tableName, String condition){
        List<List<Condition>> conditionList = Parser.parseComplexCondition(condition);
        try {
            repository.delete(tableName, conditionList);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
//        return repository.insert(tableName, conditionList);
        // вернуть сообщение об успешном удалении, если repo.insert не выбросило исключений
    }

    public void update(String tableName, String set, String where) {
        Map<String, Object> entries = Parser.parseSet(set);
//        return repository.update(tableName, entries);
        // вернуть сообщение об успешном обновлении, если repo.update не выбросило исключений
    }
}
