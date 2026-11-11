package model;

public class ForeignKey {
    private String table;
    private String column;

    public ForeignKey() {}

    public ForeignKey(String table, String column) {
        this.table = table;
        this.column = column;
    }

    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }
}
