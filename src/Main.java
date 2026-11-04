import java.util.Scanner;
import service.GoodsService;
import service.ShopsService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Консольный интерфейс СУБД.
 * Читает SQL-запросы пользователя, определяет их тип и таблицу
 * и передаёт части запроса соответствующему сервису.
 * Поддерживаются запросы SELECT, INSERT, UPDATE, DELETE, CREATE INDEX
 * и служебные команды HELP и EXIT.
 */
class CommandLineInterface {
    /** Сканер для чтения запросов с клавиатуры. */
    private final Scanner scanner;
    /** Сервис таблицы shops. */
    private final ShopsService shopsService;
    /** Сервис таблицы goods. */
    private final GoodsService goodsService;
    /** Шаблон SELECT. Группы: 1 — столбцы, 2 — таблица, 3 — условие WHERE (может отсутствовать). */
    private static final Pattern SELECT = Pattern.compile(
            "SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);
    /** Шаблон INSERT. Группы: 1 — таблица, 2 — список столбцов (может отсутствовать), 3 — значения. */
    private static final Pattern INSERT = Pattern.compile(
            "INSERT\\s+INTO\\s+(\\w+)\\s*(?:\\(([^)]*)\\))?\\s*VALUES\\s*(.+)",
            Pattern.CASE_INSENSITIVE);
    /** Шаблон UPDATE. Группы: 1 — таблица, 2 — часть SET, 3 — условие WHERE (может отсутствовать). */
    private static final Pattern UPDATE = Pattern.compile(
            "UPDATE\\s+(\\w+)\\s+SET\\s+(.+?)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);
    /** Шаблон DELETE. Группы: 1 — таблица, 2 — условие WHERE (может отсутствовать). */
    private static final Pattern DELETE = Pattern.compile(
            "DELETE\\s+FROM\\s+(\\w+)(?:\\s+WHERE\\s+(.+))?",
            Pattern.CASE_INSENSITIVE);
    /** Шаблон CREATE INDEX. Группы: 1 — имя индекса, 2 — таблица, 3 — столбец. */
    private static final Pattern CREATE_INDEX = Pattern.compile(
            "CREATE\\s+INDEX\\s+(\\w+)\\s+ON\\s+(\\w+)\\s*\\(\\s*(\\w+)\\s*\\)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Создаёт консольный интерфейс: сканер для чтения ввода
     * и сервисы таблиц goods и shops.
     */
    public CommandLineInterface() {
        this.scanner = new Scanner(System.in);
        this.shopsService = new ShopsService();
        this.goodsService = new GoodsService();
    }

    /**
     * Читает один запрос, который может занимать несколько строк.
     * Ввод заканчивается строкой, оканчивающейся на ';'.
     *
     * @return запрос без завершающей ';' и пробелов по краям,
     *         или {@code null}, если ввод закончился
     */
    private String readQuery(){
        StringBuilder sb = new StringBuilder();
        System.out.print("sql> ");
        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            sb.append(line).append(' ');
            if(line.trim().endsWith(";")){
                String q = sb.toString().trim();
                return q.substring(0, q.length() - 1).trim();
            }
            System.out.print(" -> ");
        }
        return null;
    }

    /**
     * Главный цикл программы: читает и выполняет запросы,
     * пока пользователь не введёт EXIT; или не закончится ввод.
     * Ошибки выполнения выводятся на экран, работа при этом продолжается.
     */
    public void run(){
        System.out.println("Введите SQL-запрос, в конце ;. Выход: EXIT;. Справка: HELP;");
        while (true){
            String query = readQuery();
            if(query == null || query.equalsIgnoreCase("exit")) break;
            if(query.isEmpty()) continue;
            if(query.equalsIgnoreCase("help")){
                printHelp();
                continue;
            }
            try{
                System.out.println(execute(query));
            } catch (IllegalArgumentException e){
                System.out.println("Ошибка: " + e.getMessage());
            } catch (Exception e){
                System.out.println("Внутренняя ошибка: " + e);
            }
        }
    }

    /** Выводит список поддерживаемых команд с примерами. */
    private void printHelp(){
        System.out.println("Поддерживаемые команды (каждая заканчивается ;): SELECT, UPDATE, INSERT, DELETE, CREATE INDEX");
        System.out.println("  SELECT * FROM goods; или SELECT name, price FROM goods WHERE price > 100;");
        System.out.println("  UPDATE goods SET price = 60 WHERE id = 1;");
        System.out.println("  INSERT INTO shops (id, city) VALUES (2, 'Киров'); или INSERT INTO goods VALUES (1, 'Банан', 50.0);");
        System.out.println("  DELETE FROM goods WHERE name = 'Банан';");
        System.out.println("  CREATE INDEX idx_goods_name ON goods (name);");
        System.out.println("  Доступные таблицы: goods, shops");
        System.out.println("  Вспомогательные команды: EXIT, HELP");
    }

    /**
     * Определяет тип SQL-запроса и таблицу, вызывает нужный метод сервиса.
     * Если запрос не подходит ни под один шаблон, по первому слову
     * определяет команду и сообщает пример правильного синтаксиса.
     *
     * @param query запрос без завершающей ';'
     * @return текст результата, который вернул сервис
     * @throws IllegalArgumentException если запрос не распознан
     *         или указанная таблица не найдена
     */
    private String execute(String query){
        Matcher m = SELECT.matcher(query);
        if(m.matches()){
            String columns = m.group(1);
            String table = m.group(2);
            String where = m.group(3);
            if(table.equalsIgnoreCase("goods")) return goodsService.select(columns, where);
            if(table.equalsIgnoreCase("shops")) return shopsService.select(columns, where);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = INSERT.matcher(query);
        if(m.matches()){
            String columns = m.group(2);
            String table = m.group(1);
            String values = m.group(3);
            if(table.equalsIgnoreCase("goods")) return goodsService.insert(columns, values);
            if(table.equalsIgnoreCase("shops")) return shopsService.insert(columns, values);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = UPDATE.matcher(query);
        if(m.matches()){
            String set = m.group(2);
            String table = m.group(1);
            String where = m.group(3);
            if(table.equalsIgnoreCase("goods")) return goodsService.update(set, where);
            if(table.equalsIgnoreCase("shops")) return shopsService.update(set, where);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = DELETE.matcher(query);
        if(m.matches()){
            String table = m.group(1);
            String where = m.group(2);
            if(table.equalsIgnoreCase("goods")) return goodsService.delete(where);
            if(table.equalsIgnoreCase("shops")) return shopsService.delete(where);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        m = CREATE_INDEX.matcher(query);
        if(m.matches()){
            String name = m.group(1);
            String table = m.group(2);
            String column = m.group(3);
            if(table.equalsIgnoreCase("goods")) return goodsService.createIndex(name, column);
            if(table.equalsIgnoreCase("shops")) return shopsService.createIndex(name, column);
            throw new IllegalArgumentException("Таблица не найдена: " + table);
        }
        String first = query.split("\\s+")[0].toUpperCase();
        if(first.equals("UPDATE")){
            throw new IllegalArgumentException("Неверный UPDATE. Пример: UPDATE goods SET price = 60 WHERE id = 1;");
        }
        if(first.equals("DELETE")){
            throw new IllegalArgumentException("Неверный DELETE. Пример: DELETE FROM goods WHERE name = 'Банан';");
        }
        if(first.equals("SELECT")){
            throw new IllegalArgumentException("Неверный SELECT. Пример: SELECT * FROM goods;");
        }
        if(first.equals("INSERT")){
            throw new IllegalArgumentException("Неверный INSERT. Пример: INSERT INTO shops (id, city) VALUES (2, 'Киров');");
        }
        if(first.equals("CREATE")){
            throw new IllegalArgumentException("Неверный CREATE INDEX. Пример: CREATE INDEX idx_goods_name ON goods (name);");
        }
        throw new IllegalArgumentException("Неизвестная команда: " + first + ". Введите HELP; для списка команд");
    }
}


/** Точка входа в программу. */
public class Main {
    /**
     * Запускает консольный интерфейс СУБД.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        new CommandLineInterface().run();
    }
}