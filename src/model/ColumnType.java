package model;

public enum ColumnType {
    INT,
    DOUBLE,
    STRING,
    BOOLEAN;
    public Object cast(Object v) {
        if (v == null) return null;

        switch (this) {
            case INT:
                if (v instanceof Integer) return v;
                break;
            case DOUBLE:
                if (v instanceof Number n) return n.doubleValue();
                break;
            case STRING:
                if (v instanceof String) return v;
                break;
            case BOOLEAN:
                if (v instanceof Boolean) return v;
                break;
        }
        throw new IllegalArgumentException();
    }
}
