package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Table {
    private String name;
    private List<Column> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();

    public Table() {}

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public List<String> columnNames() {
        return columns.stream().map(Column::getName).toList();
    }

    public Column column(String name) {
        return columns.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет столбца: " + name.trim()));
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Column> getColumns() { return columns; }
    public void setColumns(List<Column> columns) { this.columns = columns; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
}
