package service;

public class ShopsService {
    public String select(String columns, String where) {
        return "[заглушка] SELECT shops: columns=" + columns + ", where=" + where;
    }

    public String insert(String columns, String values) {
        return "[заглушка] INSERT shops: columns=" + columns + ", values=" + values;
    }

    public String update(String set, String where) {
        return "[заглушка] UPDATE shops: set=" + set + ", where=" + where;
    }

    public String delete(String where) {
        return "[заглушка] DELETE shops: where=" + where;
    }

    public String createIndex(String name, String column) {
        return "[заглушка] CREATE INDEX shops: name=" + name + ", column=" + column;
    }

    public String createTable(String columns) {
        return "[заглушка] CREATE TABLE shops: columns=" + columns;
    }

    public String dropTable() {
        return "[заглушка] DROP TABLE shops";
    }
}
