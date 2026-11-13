package cli;

import java.util.Scanner;

import repository.Repository;
import service.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс реализует интерфейс командной строки для взаимодействия с базой данных.
 * Позволяет вводить SQL-запросы, обрабатывать их и передавать
 * соответствующие команды в Service.
 */
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

    /**
     * Создаёт интерфейс командной строки.
     * Инициализирует Scanner для чтения пользовательского ввода
     * и Service для выполнения SQL-запросов.
     */
    public CommandLineInterface() {
        this.scanner = new Scanner(System.in);
        this.service = new Service(new Repository());
    }

    /**
     * Считывает SQL-запрос, введённый пользователем.
     * Поддерживает ввод запроса в несколько строк.
     *
     * @return считанный SQL-запрос без завершающей точки с запятой
     *         или null, если ввод завершён
     */
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

    /**
     * Запускает основной цикл работы интерфейса командной строки.
     * Считывает запросы пользователя, обрабатывает команды HELP и EXIT,
     * выполняет SQL-запросы и выводит результаты или сообщения об ошибках.
     */
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

    /**
     * Выводит справочную информацию о поддерживаемых SQL-командах
     * и примеры их использования.
     */
    private void printHelp() {
        System.out.println("Поддерживаемые SQL-команды:");
        System.out.println();
        System.out.println("1. SELECT — получение данных:");
        System.out.println("   SELECT * FROM goods;");
        System.out.println("   SELECT name, price FROM goods WHERE price > 100;");
        System.out.println();
        System.out.println("2. INSERT — добавление записи:");
        System.out.println("   INSERT INTO goods (id, name, price) VALUES (1, 'Банан', 50.0);");
        System.out.println();
        System.out.println("3. UPDATE — изменение записей:");
        System.out.println("   UPDATE goods SET price = 60 WHERE id = 1;");
        System.out.println();
        System.out.println("4. DELETE — удаление записей:");
        System.out.println("   DELETE FROM goods WHERE id = 1;");
        System.out.println();
        System.out.println("5. CREATE TABLE — создание таблицы:");
        System.out.println("   CREATE TABLE test (id INT, name STRING);");
        System.out.println();
        System.out.println("6. CREATE INDEX — создание индекса:");
        System.out.println("   CREATE INDEX idx_test ON test (id);");
        System.out.println();
        System.out.println("7. DROP TABLE — удаление таблицы:");
        System.out.println("   DROP TABLE test;");
        System.out.println();
        System.out.println("Дополнительные команды:");
        System.out.println("   HELP; — показать справку");
        System.out.println("   EXIT; — выйти из программы");
        System.out.println();
        System.out.println("Каждый SQL-запрос должен заканчиваться символом ;");
    }

    /**
     * Определяет тип SQL-запроса, проверяет его структуру
     * и передаёт соответствующую команду в Service.
     *
     * @param query SQL-запрос, введённый пользователем
     * @return результат выполнения запроса или сообщение о том,
     *         что команда пока не реализована
     * @throws IllegalArgumentException если запрос имеет неправильный
     *                                   синтаксис или содержит неизвестную команду
     */
    private String execute(String query) {
        Matcher m = SELECT.matcher(query);
        if (m.matches()) {
            String columns = m.group(1);
            String table = m.group(2);
            String where = m.group(3);
            return service.select(table, columns, where);
        }
        m = INSERT.matcher(query);
        if (m.matches()) {
            String columns = m.group(2);
            String table = m.group(1);
            String values = m.group(3);
            return service.insert(table, columns, values);
        }
        m = UPDATE.matcher(query);
        if (m.matches()) {
            String set = m.group(2);
            String table = m.group(1);
            String where = m.group(3);
            return service.update(table, set, where);
        }
        m = DELETE.matcher(query);
        if (m.matches()) {
            String table = m.group(1);
            String where = m.group(2);
            return service.delete(table, where);
        }
        m = CREATE_INDEX.matcher(query);
        if (m.matches()) {
            return "Команда CREATE INDEX пока не реализована. Ожидается реализация Service.";
        }
        m = CREATE_TABLE.matcher(query);
        if (m.matches()) {
            return "Команда CREATE TABLE пока не реализована. Ожидается реализация Service.";
        }
        m = DROP_TABLE.matcher(query);
        if (m.matches()) {
            return "Команда DROP TABLE пока не реализована. Ожидается реализация Service.";
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
