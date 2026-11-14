package model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Индекс таблицы.
 * Хранит имя индексируемого столбца и соответствие
 * между значениями этого столбца и номерами строк таблицы.
 */
public class Index {

    private String column;

    private Map<String, List<Integer>> values = new LinkedHashMap<>();

    public Index() {}

    public Index(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Map<String, List<Integer>> getValues() {
        return values;
    }

    public void setValues(Map<String, List<Integer>> values) {
        this.values = values;
    }
}