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

    /**
     * Создаёт сервис для выполнения операций над таблицами.
     * Если таблицы shops и goods отсутствуют, создаёт их
     * со стандартной структурой.
     *
     * @param repository репозиторий для хранения и получения таблиц
     */
    public Service(Repository repository) {
        this.repository = repository;
        if (!repository.exists("shops"))
            createTable("shops",
                    "id INT PRIMARY KEY, address STRING, city STRING, isOpen24h BOOLEAN");

        if (!repository.exists("goods"))
            createTable("goods",
                    "id INT PRIMARY KEY, name STRING, category STRING, price DOUBLE, shopId INT REFERENCES shops(id)");
    }

    /**
     * Возвращает список всех таблиц из репозитория.
     *
     * @return список существующих таблиц
     * @throws TableException если произошла ошибка при чтении таблиц
     */
    private List<Table> listTables() {
        try {
            return repository.listTables();
        } catch (IOException e) {
            throw new TableException("Ошибка чтения таблиц");
        }
    }

    /**
     * Выполняет запрос на выборку данных из таблицы.
     * Поддерживает выбор всех столбцов или указанных столбцов,
     * а также фильтрацию записей по условию.
     * При поиске по условию равенства может использовать индексы.
     *
     * @param tableName название таблицы
     * @param cols список выбираемых столбцов или "*" для всех столбцов
     * @param condition условие фильтрации записей
     * @return строковое представление результата запроса
     */
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

    /**
     * Проверяет, существует ли в таблице запись с таким же значением
     * первичного ключа, как у передаваемой записи.
     *
     * @param t таблица для проверки
     * @param row запись, для которой проверяется уникальность первичного ключа
     * @return true, если запись с таким первичным ключом уже существует,
     *         иначе false
     */
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

    /**
     * Добавляет новую запись в указанную таблицу.
     * Проверяет соответствие количества столбцов и значений,
     * преобразует значения к типам столбцов и проверяет уникальность
     * первичного ключа. После вставки перестраивает индексы таблицы.
     *
     * @param tableName название таблицы
     * @param cols список столбцов, в которые вставляются значения;
     *             если null, используются все столбцы таблицы
     * @param values значения новой записи
     * @return сообщение о результате операции
     */
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
        rebuildIndexes(t);
        repository.writeTable(t);
        return "Строка успешно добавлена";
    }

    /**
     * Удаляет из таблицы все записи, удовлетворяющие условию.
     * Перед удалением проверяет, не используются ли удаляемые значения
     * другими таблицами в качестве внешних ключей.
     * После удаления перестраивает индексы таблицы.
     *
     * @param tableName название таблицы
     * @param condition условие удаления записей; если null, выбираются все записи
     * @return сообщение с количеством удалённых записей
     */
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
        rebuildIndexes(t);
        repository.writeTable(t);
        return "Удалено строк: " + toDelete.size();
    }

    /**
     * Изменяет значения столбцов в записях, удовлетворяющих условию.
     * Значения преобразуются к типам соответствующих столбцов.
     * После обновления перестраивает индексы таблицы.
     *
     * @param tableName название таблицы
     * @param set выражение с новыми значениями столбцов
     * @param condition условие выбора записей для обновления;
     *                  если null, обновляются все записи
     * @return сообщение с количеством обновлённых записей
     */
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
        rebuildIndexes(t);
        repository.writeTable(t);
        return "Обновлено строк: " + count;
    }

    /**
     * Создаёт новую таблицу с указанными столбцами.
     * Разбирает определения столбцов, определяет их типы,
     * первичные ключи, ограничения NOT NULL и внешние ключи.
     *
     * @param tableName название создаваемой таблицы
     * @param columnDefs строка с определениями столбцов
     * @return сообщение об успешном создании таблицы
     */
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

    /**
     * Удаляет таблицу из репозитория.
     * Перед удалением проверяет, не ссылаются ли на неё
     * внешние ключи других таблиц.
     *
     * @param tableName название удаляемой таблицы
     * @return сообщение об успешном удалении таблицы
     */
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

    /**
     * Создаёт hash-индекс для указанного столбца таблицы.
     * Для каждого уникального значения сохраняет список номеров строк,
     * в которых встречается это значение. Значения null также индексируются.
     *
     * @param indexName название создаваемого индекса
     * @param tableName название таблицы
     * @param colName название индексируемого столбца
     * @return сообщение об успешном создании индекса
     */
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

    /**
     * Возвращает записи, которые могут удовлетворять переданным условиям.
     * Если условия позволяют использовать индекс по равенству,
     * извлекает записи по сохранённым в индексе номерам строк.
     * В остальных случаях возвращает все записи таблицы.
     *
     * @param t таблица, в которой выполняется поиск
     * @param conditions разобранные условия запроса
     * @return список записей-кандидатов для дальнейшей проверки условий
     */
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

    /**
     * Перестраивает все индексы таблицы на основе её текущих записей.
     * Очищает старые значения индексов и заново сохраняет номера строк
     * для каждого значения индексируемого столбца.
     *
     * @param t таблица, индексы которой необходимо перестроить
     */
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
