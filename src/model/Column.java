package model;

public class Column {
    private String name;
    private ColumnType type;
    private boolean primaryKey;
    private boolean notNull;
    private ForeignKey foreignKey;   // null, если ссылки нет

    public Column() {}

    public Column(String name, ColumnType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ColumnType getType() { return type; }
    public void setType(ColumnType type) { this.type = type; }
    public boolean isPrimaryKey() { return primaryKey; }
    public void setPrimaryKey(boolean primaryKey) { this.primaryKey = primaryKey; }
    public boolean isNotNull() { return notNull; }
    public void setNotNull(boolean notNull) { this.notNull = notNull; }
    public ForeignKey getForeignKey() { return foreignKey; }
    public void setForeignKey(ForeignKey foreignKey) { this.foreignKey = foreignKey; }
}
