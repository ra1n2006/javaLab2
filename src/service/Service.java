package service;

import java.io.IOException;
import java.util.*;
import config.*;
import model.Column;
import model.ColumnType;
import model.ForeignKey;
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

    public static Object parseValue(String strk) {
        strk = strk.trim();
        if (strk.startsWith("'") && strk.endsWith("'") && strk.length() >= 2)
            return strk.substring(1, strk.length() - 1);
        if (strk.contains("."))
            return Double.parseDouble(strk);
        if (strk.equalsIgnoreCase("true") || strk.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(strk);
        return Integer.parseInt(strk);
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

    public static ForeignKey parseForeignKey(String def) {
        String ref = def.substring(def.toUpperCase().indexOf("REFERENCES") + 10).trim();   // "shops(id)"
        if (!ref.contains("(") || !ref.contains(")"))
            throw new IllegalArgumentException("Ожидается REFERENCES таблица(столбец)");

        String table = ref.substring(0, ref.indexOf('(')).trim();                          // "shops"
        String column = ref.substring(ref.indexOf('(') + 1, ref.indexOf(')')).trim();      // "id"
        return new ForeignKey(table, column);
    }
}

public class Service {
    private Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
        initDefaultTables();
    }

    private void initDefaultTables() {
        if (!repository.exists("shops"))
            createTable("shops",
                    "id INT PRIMARY KEY, address STRING, city STRING, isOpen24h BOOLEAN");

        if (!repository.exists("goods"))
            createTable("goods",
                    "id INT PRIMARY KEY, name STRING, category STRING, price DOUBLE, shopId INT REFERENCES shops(id)");
    }

    public String select(String tableName, String cols, String condition) {
        List<String> colsList = new ArrayList<>();
        List<List<Condition>> conditionList = new ArrayList<>();
        final Table t;
        try {
            t = repository.getTable(tableName);
        }catch (Exception e) {
            return "таблицы не существует";
        }
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
        Table t = new Table();
        try {
            t = repository.getTable(tableName);
        }catch (Exception e) {
            e.printStackTrace();
            return "таблицы не существует";
        }
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
        try {
            repository.writeTable(t);
        }catch (IOException e) {
            return "ошибка при записи данных";
        }
        return "Строка успешно добавлена";
    }

    public String delete(String tableName, String condition) {
        Table t = new Table();
        try {
            t = repository.getTable(tableName);
        }catch (Exception e) {
            return "таблицы не существует";
        }
        List<List<Condition>> conditionList = (condition == null)
                ? new ArrayList<>()
                : Parser.parseComplexCondition(condition);

        t.getRows().removeIf(row -> Condition.matches(row, conditionList));
        try {
            repository.writeTable(t);
        }catch (IOException e) {
            return "ошибка при удалении данных";
        }
        return "Операция удаления выполнена";
    }

    public String update(String tableName, String set, String condition) {
        final Table t;
        try {
            t = repository.getTable(tableName);
        } catch (Exception e) {
            return "таблицы не существует";
        }

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
        try {
            repository.writeTable(t);
        }catch (IOException e) {
            return "ошибка при удалении данных";
        }
        return "Обновлено строк: " + count;
    }

    public String createTable(String tableName, String columnDefs) {
        if (repository.exists(tableName))
            throw new IllegalArgumentException("Таблица уже существует: " + tableName);

        List<Column> columns = new ArrayList<>();
        for (String def : columnDefs.split(",")) {
            String[] w = def.trim().split("\\s+");
            if (w.length < 2)
                throw new IllegalArgumentException("Неверное описание столбца: " + def.trim());

            Column col = new Column(w[0], ColumnType.valueOf(w[1].toUpperCase()));
            String rest = def.toUpperCase();

            if (rest.contains("PRIMARY KEY")) {
                col.setPrimaryKey(true);
                col.setNotNull(true);
            }
            if (rest.contains("NOT NULL")) col.setNotNull(true);
            if (rest.contains("REFERENCES")) col.setForeignKey(Parser.parseForeignKey(def));

            columns.add(col);
        }

        try {
            repository.createTable(new Table(tableName, columns));
        } catch (IOException e) {
            return "ошибка при записи данных";
        }
        return "Таблица " + tableName + " создана";
    }
}
