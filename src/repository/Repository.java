package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.Table;

import java.io.File;
import java.io.IOException;

public class Repository {

    private final ObjectMapper objectMapper;

    public Repository() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public void createTable(Table table) throws IOException {
        new File("data").mkdirs();
        String fileName = "data/" + table.getName() + ".json";

        objectMapper.writeValue(new File(fileName), table);
    }

    public Table getTable(String tableName) throws IOException {

        String fileName = "data/" + tableName + ".json";

        return objectMapper.readValue(new File(fileName), Table.class);
    }

    public void deleteTable(String tableName) {

        String fileName = "data/" + tableName + ".json";

        File file = new File(fileName);

        if (!file.delete()) {
            throw new IllegalArgumentException("Не удалось удалить таблицу: " + tableName);
        }
    }

    public void writeTable(Table table) throws IOException {

        String fileName = "data/" + table.getName() + ".json";

        objectMapper.writeValue(new File(fileName), table);
    }

    public boolean exists(String tableName) {
        return new File("data/" + tableName + ".json").exists();
    }
}