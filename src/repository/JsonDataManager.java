package repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JsonDataManager {

    private final ObjectMapper objectMapper;

    public JsonDataManager() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public <T>  Map<String, T> read(String file, Class<T> type) throws IOException {
        return objectMapper.readValue(
                new File(file),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, type)
        );
    }

    public <T> void write(String file, Map<String, T> data) throws IOException {
        objectMapper.writeValue(new File(file), data);
    }

    public Map<String, Object> convertToMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }
}