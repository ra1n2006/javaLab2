package model;

import exception.TypeException;

/**
 * Определяет поддерживаемые типы данных столбцов таблицы.
 */
public enum ColumnType {
    INT,
    DOUBLE,
    STRING,
    BOOLEAN;

    /**
     * Приводит переданное значение к типу текущего перечисления.
     * Значение null возвращается без изменений.
     *
     * @param v значение для преобразования
     * @return значение, соответствующее типу текущего перечисления
     * @throws TypeException если значение нельзя привести к требуемому типу
     */
    public Object cast(Object v) throws TypeException {
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
        throw new TypeException("Ожидается " + this + ", получено: " + v
                + " (" + v.getClass().getSimpleName() + ")");
    }
}
