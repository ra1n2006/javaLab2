package service;

import java.io.IOException;
import java.util.*;
import config.*;
import exception.ConstraintException;
import exception.SqlSyntaxException;
import exception.TableException;
import model.Column;
import model.ColumnType;
import model.ForeignKey;
import model.Table;
import model.Index;
import repository.Repository;


public class Service {
    private final Repository repository;

    public Service(Repository repository) {
        this.repository = repository;
        if (!repository.exists("shops"))
            createTable("shops",
                    "id INT PRIMARY KEY, address STRING, city STRING, isOpen24h BOOLEAN");

        if (!repository.exists("goods"))
            createTable("goods",
                    "id INT PRIMARY KEY, name STRING, category STRING, price DOUBLE, shopId INT REFERENCES shops(id)");
    }

    private List<Table> listTables() {
        try {
            return repository.listTables();
        } catch (IOException e) {
            throw new TableException("Ошибка чтения таблиц");
        }
    }

    public String select(String tableName, String cols, String condition) {
        final List<String> colsList;
        List<List<Condition>> conditionList = new ArrayList<>();
        final Table t = repository.getTable(tableName);
        if (cols.trim().equals("*")) {
            colsList = t.columnNames();
        } else {
            colsList = Arrays.stream(cols.split(",")).map(c -> t.column(c).getName()).toList();
        }
        if (condition != null) {
            conditionList = Parser.parseComplexCondition(condition);
        }
        List<Map<String, Object>> rows = findRows(t, conditionList);
        String header = String.join(" | ", colsList);
        StringBuilder sb = new StringBuilder(header).append('\n');
        sb.append("-".repeat(header.length())).append('\n');
        for (Map<String, Object> row : rows) {
            if (!Condition.matches(row, conditionList)) continue;
            for (String col : colsList) {
                sb.append(row.get(col)).append(" | ");
            }
            sb.setLength(sb.length() - 3);
            sb.append('\n');
        }
        return sb.toString();
    }

    private boolean findEqualPrimaryKey(Table t, Map<String, Object> row) {
        for (Column c : t.getColumns()) {
            if (c.isPrimaryKey()) {
                Object newValue = row.get(c.getName());
                for (Map<String, Object> existing : t.getRows()) {
                    if (newValue != null && newValue.equals(existing.get(c.getName()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String insert(String tableName, String cols, String values) {
        final Table t = repository.getTable(tableName);
        List<String> names = (cols == null)
                ? t.columnNames()
                : Arrays.stream(cols.split(",")).map(String::trim).toList();

        String[] vals = values.trim().substring(1, values.trim().length() - 1).split(",");
        if (vals.length != names.size())
            throw new SqlSyntaxException("Ошибка в написании запроса: столбцов " + names.size() + ", значений " + vals.length);

        Map<String, Object> row = new LinkedHashMap<>();
        for (String c : t.columnNames()) row.put(c, null);
        for (int i = 0; i < vals.length; i++) {
            Column col = t.column(names.get(i));
            row.put(col.getName(), col.getType().cast(Parser.parseValue(vals[i])));
        }

        if (findEqualPrimaryKey(t, row))
            throw new ConstraintException("Такой первичный ключ уже существует");

        t.getRows().add(row);
        try {
            rebuildIndexes(t);
            repository.writeTable(t);
        }catch (IOException e) {
            return "ошибка при записи данных";
        }
        return "Строка успешно добавлена";
    }

    public String delete(String tableName, String condition) {
        final Table t = repository.getTable(tableName);
        List<List<Condition>> conditionList = (condition == null)
                ? new ArrayList<>()
                : Parser.parseComplexCondition(condition);

        List<Map<String, Object>> toDelete = new ArrayList<>();
        for (Map<String, Object> row : t.getRows()) {
            if (Condition.matches(row, conditionList)) toDelete.add(row);
        }

        for (Table other : listTables()) {
            for (Column c : other.getColumns()) {
                ForeignKey fk = c.getForeignKey();
                if (fk == null || !fk.table().equalsIgnoreCase(tableName)) continue;

                for (Map<String, Object> parentRow : toDelete) {
                    Object parentValue = parentRow.get(fk.column());
                    for (Map<String, Object> childRow : other.getRows()) {
                        if (parentValue != null && parentValue.equals(childRow.get(c.getName()))) {
                            throw new ConstraintException("Нельзя удалить: ключ таблицы является внешним ключом другой таблицы");
                        }
                    }
                }
            }
        }

        t.getRows().removeAll(toDelete);
        repository.writeTable(t);
        try {
            rebuildIndexes(t);
            repository.writeTable(t);
        } catch (IOException e) {
            return "ошибка при удалении данных";
        }
        return "Удалено строк: " + toDelete.size();
    }

    public String update(String tableName, String set, String condition) {
        final Table t = repository.getTable(tableName);

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
            rebuildIndexes(t);
            repository.writeTable(t);
        }catch (IOException e) {
            return "ошибка при удалении данных";
        }
        return "Обновлено строк: " + count;
    }

    public String createTable(String tableName, String columnDefs) {
        if (repository.exists(tableName))
            throw new TableException("Таблица уже существует: " + tableName);

        List<Column> columns = new ArrayList<>();
        for (String def : columnDefs.split(",")) {
            String[] w = def.trim().split("\\s+");
            if (w.length < 2)
                throw new SqlSyntaxException("В описании столбца нужно минимум два аргумента: имя и тип");

            final ColumnType type;
            try {
                type = ColumnType.valueOf(w[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new SqlSyntaxException("Неизвестный тип: " + w[1]);
            }
            Column col = new Column(w[0], type);
            String rest = def.toUpperCase();

            if (rest.contains("PRIMARY KEY")) {
                col.setPrimaryKey(true);
                col.setNotNull(true);
            }
            if (rest.contains("NOT NULL")) col.setNotNull(true);
            if (rest.contains("REFERENCES")) col.setForeignKey(Parser.parseForeignKey(def));

            columns.add(col);
        }

        repository.createTable(new Table(tableName, columns));
        return "Таблица " + tableName + " создана";
    }

    public String dropTable(String tableName) {
        if (!repository.exists(tableName))
            throw new TableException("Таблица не найдена: " + tableName);

        for (Table t : listTables()) {
            if (t.getName().equalsIgnoreCase(tableName)) continue;
            for (Column c : t.getColumns()) {
                if (c.getForeignKey() != null && c.getForeignKey().table().equalsIgnoreCase(tableName)) {
                    throw new ConstraintException("Нельзя удалить: на таблицу ссылается " + t.getName() + "." + c.getName());
                }
            }
        }
        repository.deleteTable(tableName);
        return "Таблица успешно удалена";
    }

    public String createIndex(String indexName, String tableName, String colName) {
        final Table t = repository.getTable(tableName);
        Column col = t.column(colName);
        if (t.getIndexes().containsKey(indexName)) {
            throw new TableException(
                    "Индекс уже существует: " + indexName
            );
        }
        Index index = new Index(col.getName());

        for (int i = 0; i < t.getRows().size(); i++) {
            Map<String, Object> row = t.getRows().get(i);
            Object value = row.get(col.getName());
            String key = value == null
                    ? "__NULL__"
                    : String.valueOf(value);
            index.getValues()
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(i);
        }
        t.getIndexes().put(indexName, index);
        repository.writeTable(t);
        return "Индекс создан";
    }

    private List<Map<String, Object>> findRows(
            Table t,
            List<List<Condition>> conditions
    ) {
        if (conditions.size() != 1) {
            return t.getRows();
        }
        for (Condition c : conditions.get(0)) {
            boolean isEquals =
                    c.getOp() == Operator.EQ ||
                            c.getOp() == Operator.EQS;
            if (!isEquals) {
                continue;
            }
            for (Index index : t.getIndexes().values()) {
                if (!index.getColumn().equalsIgnoreCase(c.getCol())) {
                    continue;
                }
                Object value = t.column(c.getCol())
                        .getType()
                        .cast(c.getValue());
                String key = value == null
                        ? "__NULL__"
                        : String.valueOf(value);
                List<Integer> rowNumbers =
                        index.getValues().get(key);
                if (rowNumbers == null) {
                    return new ArrayList<>();
                }
                List<Map<String, Object>> result =
                        new ArrayList<>();
                for (Integer rowNumber : rowNumbers) {
                    result.add(t.getRows().get(rowNumber));
                }
                return result;
            }
        }
        return t.getRows();
    }

    private void rebuildIndexes(Table t) {
        for (Index index : t.getIndexes().values()) {
            index.getValues().clear();
            for (int i = 0; i < t.getRows().size(); i++) {
                Map<String, Object> row = t.getRows().get(i);
                Object value = row.get(index.getColumn());
                String key = value == null
                        ? "__NULL__"
                        : String.valueOf(value);
                index.getValues()
                        .computeIfAbsent(key, k -> new ArrayList<>())
                        .add(i);
            }
        }
    }
}
