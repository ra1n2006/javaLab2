package config;

import exception.TypeException;

import java.util.List;
import java.util.Map;

/**
 * Представляет одно условие сравнения значения столбца с заданным значением.
 * Также содержит методы для проверки одного условия и группы условий.
 */
public class Condition {
    private final String col;
    private final Operator op;
    private final Object value;

    /**
     * Создаёт условие сравнения.
     *
     * @param col имя столбца
     * @param op оператор сравнения
     * @param value значение для сравнения
     */
    public Condition(String col, Operator op, Object value) {
        this.col = col;
        this.op = op;
        this.value = value;
    }

    /**
     * Возвращает оператор сравнения.
     *
     * @return оператор условия
     */
    public Operator getOp() {
        return op;
    }

    /**
     * Возвращает значение, с которым сравнивается значение столбца.
     *
     * @return значение условия
     */
    public Object getValue() {
        return value;
    }

    /**
     * Возвращает имя столбца, к которому применяется условие.
     *
     * @return имя столбца
     */
    public String getCol() {
        return col;
    }

    /**
     * Проверяет, выполняется ли условие для переданного значения.
     * Для строкового оператора EQS используется точное сравнение строк.
     * Для остальных операторов выполняется сравнение чисел, логических
     * значений или строк.
     *
     * @param actual фактическое значение столбца
     * @return true, если условие выполняется, иначе false
     * @throws TypeException если типы фактического и заданного значений несовместимы
     */
    private boolean result(Object actual) {
        if (actual == null) return false;

        if (op == Operator.EQS)
            return actual instanceof String a && value instanceof String b && a.equals(b);

        int c;
        if (actual instanceof Number a && value instanceof Number b)
            c = Double.compare(a.doubleValue(), b.doubleValue());
        else if (actual instanceof Boolean a && value instanceof Boolean b)
            c = Boolean.compare(a, b);
        else if (actual instanceof String a && value instanceof String b)
            c = a.compareTo(b);
        else
            throw new TypeException("Столбец и значение имеют разные типы");


        return switch (op) {
            case EQ -> c == 0;
            case G  -> c > 0;
            case L  -> c < 0;
            case GE -> c >= 0;
            case LE -> c <= 0;
            case EQS -> false;
        };
    }

    /**
     * Проверяет, соответствует ли строка набору условий.
     * Условия внутри одной группы объединяются логическим И (AND),
     * а разные группы — логическим ИЛИ (OR).
     * Если список условий пуст, строка считается подходящей.
     *
     * @param row строка таблицы в виде отображения имени столбца на значение
     * @param where группы условий для проверки
     * @return true, если строка удовлетворяет хотя бы одной группе условий
     */
    public static boolean matches(Map<String, Object> row, List<List<Condition>> where) {
        if (where.isEmpty()) return true;

        for (List<Condition> group : where) {
            boolean groupMatch = true;
            for (Condition cond : group) {
                if (!cond.result(row.get(cond.col))) {
                    groupMatch = false;
                    break;
                }
            }
            if (groupMatch) return true;
        }
        return false;
    }
}
