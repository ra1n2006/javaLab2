package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Репозиторий для работы с таблицами, хранящимися в JSON-файлах.
 * <p>
 * Каждая таблица хранится в отдельном файле с именем,
 * соответствующим имени таблицы.
 * Например, таблица с именем {@code goods} хранится в файле
 * {@code data/goods.json}.
 */
public class Repository {

    /**
     * Объект Jackson для преобразования объектов Java в JSON
     * и JSON в объекты Java.
     */
    private final ObjectMapper objectMapper;

    /**
     * Создаёт репозиторий и настраивает {@link ObjectMapper}
     * для работы с датами и временем Java.
     */
    public Repository() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Создаёт JSON-файл для новой таблицы и записывает
     * в него переданный объект {@link Table}.
     *
     * @param table таблица, которую необходимо сохранить
     * @throws IOException если произошла ошибка при создании
     *                     или записи файла
     */
    public void createTable(Table table) throws IOException {
        new File("data").mkdirs();
        String fileName = "data/" + table.getName() + ".json";

        objectMapper.writeValue(new File(fileName), table);
    }

    /**
     * Читает таблицу из соответствующего JSON-файла
     * и преобразует её в объект {@link Table}.
     *
     * @param tableName имя таблицы
     * @return объект {@link Table}, прочитанный из JSON-файла
     * @throws IOException если файл не существует или произошла
     *                     ошибка при чтении
     */
    public Table getTable(String tableName) throws IOException {

        String fileName = "data/" + tableName + ".json";

        return objectMapper.readValue(new File(fileName), Table.class);
    }

    /**
     * Удаляет JSON-файл, соответствующий указанной таблице.
     *
     * @param tableName имя таблицы, которую необходимо удалить
     * @throws IllegalArgumentException если файл таблицы не удалось удалить
     */
    public void deleteTable(String tableName) {

        String fileName = "data/" + tableName + ".json";

        File file = new File(fileName);

        if (!file.delete()) {
            throw new IllegalArgumentException("Не удалось удалить таблицу: " + tableName);
        }
    }

    /**
     * Перезаписывает JSON-файл существующей таблицы
     * новой версией объекта {@link Table}.
     * <p>
     * Используется после изменения содержимого таблицы,
     * например после добавления или обновления строки.
     *
     * @param table новая версия таблицы, которую необходимо сохранить
     * @throws IOException если произошла ошибка при записи файла
     */
    public void writeTable(Table table) throws IOException {

        String fileName = "data/" + table.getName() + ".json";

        objectMapper.writeValue(new File(fileName), table);
    }

    public List<Table> listTables() throws IOException {
        List<Table> tables = new ArrayList<>();
        File[] files = new File("data").listFiles();
        if (files == null) return tables;                       // папки data ещё нет

        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                tables.add(objectMapper.readValue(file, Table.class));
            }
        }
        return tables;
    }

    public boolean exists(String tableName) {
        return new File("data/" + tableName + ".json").exists();
    }
}