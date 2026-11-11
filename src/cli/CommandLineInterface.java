package cli;

import java.util.Scanner;

import repository.Repository;
import service.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandLineInterface {

    private final Scanner scanner;
    private final Service service;

    private static final Pattern SELECT = Pattern.compile(
            "SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern INSERT = Pattern.compile(
            "INSERT\\s+INTO\\s+(\\w+)\\s*(?:\\(([^)]*)\\))?\\s*VALUES\\s*(.+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern UPDATE = Pattern.compile(
            "UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DELETE = Pattern.compile(
            "DELETE\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CREATE_INDEX = Pattern.compile(
            "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+(\\w+)\\s*\\(\\s*(\\w+)\\s*\\)",
            Pattern.CASE_INSENSITIVE);


    private static final Pattern CREATE_TABLE = Pattern.compile(
            "CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.+)\\)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern DROP_TABLE = Pattern.compile(
            "DROP\\s+TABLE\\s+(\\w+)",
            Pattern.CASE_INSENSITIVE);


    public CommandLineInterface() {
        this.scanner = new Scanner(System.in);
        this.service = new Service(new Repository());
    }

    private String readQuery() {
        StringBuilder sb = new StringBuilder();
        System.out.print("sql> ");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            sb.append(line).append(' ');
            if (line.trim().endsWith(";")) {
                String q = sb.toString().trim();
                return q.substring(0, q.length() - 1).trim();
            }
            System.out.print(" -> ");
        }
        return null;
    }

    public void run() {
        System.out.println("Введите SQL-запрос, в конце ;. Выход: EXIT;. Справка: HELP;");
        while (true) {
            String query = readQuery();
            if (query == null || query.equalsIgnoreCase("exit")) break;
            if (query.isEmpty()) continue;
            if (query.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            try {
                System.out.println(execute(query));
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Внутренняя ошибка: " + e);
            }
        }
    }

    private void printHelp() {
        System.out.println("Поддерживаемые команды (каждая заканчивается ;): SELECT, UPDATE, INSERT, DELETE, CREATE INDEX, CREATE TABLE, DROP TABLE");
        System.out.println("  SELECT * FROM goods; или SELECT name, price FROM goods WHERE price > 100;");
        System.out.println("  UPDATE goods SET price = 60 WHERE id = 1;");
        System.out.println("  INSERT INTO shops (id, city) VALUES (2, 'Киров'); или INSERT INTO goods VALUES (1, 'Банан', 50.0);");
        System.out.println("  DELETE FROM goods WHERE name = 'Банан';");
        System.out.println("  CREATE INDEX idx_goods_name ON goods (name);");
        System.out.println("  CREATE TABLE goods (id INT, name STRING, price DOUBLE);");
        System.out.println("  DROP TABLE goods;");
        System.out.println("  Доступные таблицы: goods, shops");
        System.out.println("  Вспомогательные команды: EXIT, HELP");
    }

    private String execute(String query) {
        Matcher m = SELECT.matcher(query);
        if (m.matches()) {
            String columns = m.group(1);
            String table = m.group(2);
            String where = m.group(3);
            service.select(table, columns, where);
//            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = INSERT.matcher(query);
        if (m.matches()) {
            String columns = m.group(2);
            String table = m.group(1);
            String values = m.group(3);
            service.insert(table, columns, values);
//            if (table.equalsIgnoreCase("goods")) return goodsService.insert(columns, values);
//            if (table.equalsIgnoreCase("shops")) return shopsService.insert(columns, values);
//            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = UPDATE.matcher(query);
        if (m.matches()) {
            String set = m.group(2);
            String table = m.group(1);
            String where = m.group(3);
            service.update(table, set, where);
//            if (table.equalsIgnoreCase("goods")) return goodsService.update(set, where);
//            if (table.equalsIgnoreCase("shops")) return shopsService.update(set, where);
//            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = DELETE.matcher(query);
        if (m.matches()) {
            String table = m.group(1);
            String where = m.group(2);
            service.delete(table, where);
//            if (table.equalsIgnoreCase("goods")) return goodsService.delete(where);
//            if (table.equalsIgnoreCase("shops")) return shopsService.delete(where);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = CREATE_INDEX.matcher(query);
        if (m.matches()) {
            String name = m.group(1);
            String table = m.group(2);
            String column = m.group(3);
//            if (table.equalsIgnoreCase("goods")) return goodsService.createIndex(name, column);
//            if (table.equalsIgnoreCase("shops")) return shopsService.createIndex(name, column);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = CREATE_TABLE.matcher(query);
        if (m.matches()) {
            String table = m.group(1);
            String columns = m.group(2);
//            if (table.equalsIgnoreCase("goods")) return goodsService.createTable(columns);
//            if (table.equalsIgnoreCase("shops")) return shopsService.createTable(columns);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = DROP_TABLE.matcher(query);
        if (m.matches()) {
            String table = m.group(1);
//            if (table.equalsIgnoreCase("goods")) return goodsService.dropTable();
//            if (table.equalsIgnoreCase("shops")) return shopsService.dropTable();
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        String[] words = query.split("\\s+");
        String first = words[0].toUpperCase();
        String second = words.length > 1 ? words[1].toUpperCase() : "";
        switch (first) {
            case "UPDATE":
                throw new IllegalArgumentException("Неверный UPDATE. Пример: UPDATE goods SET price = 60 WHERE id = 1;");
            case "DELETE":
                throw new IllegalArgumentException("Неверный DELETE. Пример: DELETE FROM goods WHERE name = 'Банан';");
            case "SELECT":
                throw new IllegalArgumentException("Неверный SELECT. Пример: SELECT * FROM goods;");
            case "INSERT":
                throw new IllegalArgumentException("Неверный INSERT. Пример: INSERT INTO shops (id, city) VALUES (2, 'Киров');");
            case "CREATE":
                if (second.equals("INDEX"))
                    throw new IllegalArgumentException("Неверный CREATE INDEX. Пример: CREATE INDEX idx_goods_name ON goods (name);");
                if (second.equals("TABLE"))
                    throw new IllegalArgumentException("Неверный CREATE TABLE. Пример: CREATE TABLE goods (id INT, name STRING, price DOUBLE);");
                throw new IllegalArgumentException("После CREATE ожидается TABLE или INDEX");
            case "DROP":
                throw new IllegalArgumentException("Неверный DROP TABLE. Пример: DROP TABLE goods;");
            default:
                throw new IllegalArgumentException("Неизвестная команда: " + first + ". Введите HELP; для списка команд");
        }
    }
}
