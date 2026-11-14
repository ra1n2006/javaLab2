package service;

import config.Condition;
import config.Operator;
import exception.SqlSyntaxException;
import exception.TypeException;
import model.ForeignKey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {

    /**
     * Разбирает одиночное условие сравнения и преобразует его
     * в объект Condition.
     * Определяет оператор сравнения и преобразует значение
     * правой части условия.
     *
     * @param cond строка с условием сравнения
     * @return объект Condition с названием столбца, оператором и значением
     * @throws SqlSyntaxException если условие содержит неподдерживаемый оператор
     */
    public static Condition parseSingleCondition(String cond) {
        for (String sign : new String[]{">=", "<=", "=", ">", "<"}) {
            int i = cond.indexOf(sign);
            if (i > 0) {
                String raw = cond.substring(i + sign.length()).trim();
                Operator op = switch (sign) {
                    case "="  -> raw.startsWith("'") ? Operator.EQS : Operator.EQ;
                    case ">=" -> Operator.GE;
                    case "<=" -> Operator.LE;
                    case ">"  -> Operator.G;
                    default   -> Operator.L;
                };
                return new Condition(cond.substring(0, i).trim(), op, parseValue(raw));
            }
        }
        throw new SqlSyntaxException("Не поддерживаемый знак условия");
    }

    /**
     * Разбирает составное условие WHERE на отдельные условия.
     * Условия, объединённые оператором AND, помещаются в одну группу,
     * а группы, объединённые оператором OR, разделяются.
     *
     * @param where строка с составным условием WHERE
     * @return список групп условий, где каждая группа содержит условия AND
     */
    public static List<List<Condition>> parseComplexCondition(String where) {

        return Arrays.stream(where.split("(?i)\\s+OR\\s+"))
                .map(orPart -> Arrays.stream(orPart.split("(?i)\\s+AND\\s+"))
                        .map(Parser::parseSingleCondition)
                        .toList())
                .toList();
    }

    /**
     * Преобразует строковое значение в соответствующий тип Java.
     * Строки в одинарных кавычках преобразуются в String,
     * логические значения — в Boolean, числа — в Integer или Double.
     *
     * @param row строковое представление значения
     * @return значение соответствующего типа
     * @throws TypeException если значение не удалось преобразовать в число
     */
    public static Object parseValue(String row) {
        row = row.trim();
        if (row.startsWith("'") && row.endsWith("'") && row.length() >= 2)
            return row.substring(1, row.length() - 1);
        if (row.equalsIgnoreCase("true") || row.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(row);
        try {
            if (row.contains("."))
                return Double.parseDouble(row);
            return Integer.parseInt(row);
        } catch (NumberFormatException e) {
            throw new TypeException("Неверное значение: " + row);
        }
    }

    /**
     * Разбирает выражение SET и извлекает пары
     * «название столбца — новое значение».
     * Несколько присваиваний разделяются запятыми.
     *
     * @param set строка с выражениями присваивания
     * @return отображение названий столбцов на новые значения
     * @throws SqlSyntaxException если в присваивании отсутствует знак равно
     */
    public static Map<String, Object> parseSet(String set) {
        Map<String, Object> table = new HashMap<>();
        for (String part : set.split(",")) {
            int i = part.indexOf('=');
            if (i < 0) throw new SqlSyntaxException("Неверное присваивание, должен быть знак равно");
            table.put(part.substring(0, i).trim(), parseValue(part.substring(i + 1)));
        }
        return table;
    }

    /**
     * Извлекает информацию о внешнем ключе из определения столбца.
     * Определяет название связанной таблицы и столбца.
     *
     * @param def определение столбца, содержащее REFERENCES
     * @return объект ForeignKey с названием таблицы и столбца
     * @throws SqlSyntaxException если ссылка не содержит круглых скобок
     */
    public static ForeignKey parseForeignKey(String def) {
        String ref = def.substring(def.toUpperCase().indexOf("REFERENCES") + 10).trim();
        if (!ref.contains("(") || !ref.contains(")"))
            throw new SqlSyntaxException("Внешний ключ должен быть в круглых скобках");

        String table = ref.substring(0, ref.indexOf('(')).trim();
        String column = ref.substring(ref.indexOf('(') + 1, ref.indexOf(')')).trim();
        return new ForeignKey(table, column);
    }
}
