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

    public static List<List<Condition>> parseComplexCondition(String where) {

        return Arrays.stream(where.split("(?i)\\s+OR\\s+"))
                .map(orPart -> Arrays.stream(orPart.split("(?i)\\s+AND\\s+"))
                        .map(Parser::parseSingleCondition)
                        .toList())
                .toList();
    }

    public static Object parseValue(String row) {
        row = row.trim();
        if (row.startsWith("'") && row.endsWith("'") && row.length() >= 2)
            return row.substring(1, row.length() - 1);
        if (row.equalsIgnoreCase("true") || row.equalsIgnoreCase("false"))
            return Boolean.parseBoolean(row);
        try {
            return row.contains(".") ? Double.parseDouble(row) : Integer.parseInt(row);
        } catch (NumberFormatException e) {
            throw new TypeException("Неверное значение: " + row);
        }
    }

    public static Map<String, Object> parseSet(String set) {
        Map<String, Object> table = new HashMap<>();
        for (String part : set.split(",")) {
            int i = part.indexOf('=');
            if (i < 0) throw new SqlSyntaxException("Неверное присваивание, должен быть знак равно");
            table.put(part.substring(0, i).trim(), parseValue(part.substring(i + 1)));
        }
        return table;
    }

    public static ForeignKey parseForeignKey(String def) {
        String ref = def.substring(def.toUpperCase().indexOf("REFERENCES") + 10).trim();
        if (!ref.contains("(") || !ref.contains(")"))
            throw new SqlSyntaxException("Внешний ключ должен быть в круглых скобках");

        String table = ref.substring(0, ref.indexOf('(')).trim();
        String column = ref.substring(ref.indexOf('(') + 1, ref.indexOf(')')).trim();
        return new ForeignKey(table, column);
    }
}
