package service;

import java.util.*;
import config.*;
import model.Column;
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

    public String select(String tableName, String cols, String condition) {
        List<String> colsList = new ArrayList<>();
        List<List<Condition>> conditionList = new ArrayList<>();
        Table t = repository.load(tableName);
        if (cols.trim().equals("*")) {
            colsList = t.columnNames();
        } else {
            colsList = Arrays.stream(cols.split(",")).map(c -> t.column(c).getName()).toList();
        }
        if (condition != null) {
            conditionList = Parser.parseComplexCondition(condition);
        }

        StringBuilder sb = new StringBuilder(String.join(" | ", colsList)).append('\n');
        int count = 0;
        for (Map<String, Object> row : t.getRows()) {
            if (!Condition.matches(row, conditionList)) continue;
            for (String col : colsList) {
                sb.append(row.get(col)).append(" | ");
            }
            sb.setLength(sb.length() - 3);
            sb.append('\n');
            count++;
        }
        return sb.append("Строк: ").append(count).toString();
    }

    public String insert(String tableName, String cols, String values) throws IllegalArgumentException {
        Table t = repository.load(tableName);
        List<String> names = (cols == null)
                ? t.columnNames()
                : Arrays.stream(cols.split(",")).map(String::trim).toList();

        String[] vals = values.trim().substring(1, values.trim().length() - 1).split(",");
        if (vals.length != names.size())
            throw new IllegalArgumentException("Ошибка в написании запроса: столбцов " + names.size() + ", значений " + vals.length);

        Map<String, Object> row = new LinkedHashMap<>();
        for (String c : t.columnNames()) row.put(c, null);
        for (int i = 0; i < vals.length; i++) {
            Column col = t.column(names.get(i));
            row.put(col.getName(), col.getType().cast(Parser.parseValue(vals[i])));
        }

        t.getRows().add(row);
        repository.save(t);
        return "Строка успешно добавлена";
    }

    public String delete(String tableName, String condition) {
        Table t = repository.load(tableName);
        List<List<Condition>> conditionList = (condition == null)
                ? new ArrayList<>()
                : Parser.parseComplexCondition(condition);

        t.getRows().removeIf(row -> Condition.matches(row, conditionList));
        repository.save(t);
        return "Операция удаления выполнена";
    }

    public String update(String tableName, String set, String condition) {
        Table t = repository.load(tableName);

        Map<String, Object> changes = new LinkedHashMap<>();
        Parser.parseSet(set).forEach((name, v) -> {
            Column col = t.column(name);
            changes.put(col.getName(), col.getType().cast(v));
        });

        List<List<Condition>> conditionList = new ArrayList<>();
        if (condition != null) {
            conditionList = Parser.parseComplexCondition(condition);
        }

        int count = 0;
        for (Map<String, Object> row : t.getRows()) {
            if (!Condition.matches(row, conditionList)) continue;
            row.putAll(changes);
            count++;
        }
        repository.save(t);
        return "Обновлено строк: " + count;
    }
}
