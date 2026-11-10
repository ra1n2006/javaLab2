package repository;

import model.Good;
import model.Shop;

import java.util.*;
import java.io.IOException;
import service.Condition;

import service.GoodsService;



public class Repository {


    public List<?> select(String tableName, List<List<Condition>> where) throws IOException {

        String fileName = "data/" + tableName.toLowerCase() + ".json";

        Map<String, ?> data;

        JsonDataManager jsonDataManager = new JsonDataManager();

        if (tableName.equalsIgnoreCase("goods")) {
            data = jsonDataManager.read(fileName, Good.class);
        } else if (tableName.equalsIgnoreCase("shops")) {
            data = jsonDataManager.read(fileName, Shop.class);
        } else {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }

        List<Object> result = new ArrayList<>();

        for (Map.Entry<String, ?> entry : data.entrySet()) {

            Object object = entry.getValue();

            Integer id = Integer.valueOf(entry.getKey());

            Map<String, Object> row = jsonDataManager.convertToMap(object);
            row.put("id", id);

            if (object instanceof Good good) {
                good.setId(id);
            } else if (object instanceof Shop shop) {
                shop.setId(id);
            }

            if (Condition.matches(row, where)) {
                result.add(object);
            }
        }

        return result;
    }

    public void insert(String tableName, Map<String, Object> data) throws IOException {

        JsonDataManager jsonDataManager = new JsonDataManager();

        String fileName = "data/" + tableName.toLowerCase() + ".json";

        if (!tableName.equals("goods") && !tableName.equals("shops")) {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }

        List<String> columns = getColumns(tableName);

        for (String column : data.keySet()) {
            if (!columns.contains(column)) {
                throw new IllegalArgumentException("Unknown column: " + column);
            }
        }

        Integer id = (Integer) data.get("id");

        if (id == null) {
            throw new IllegalArgumentException("ID is required");
        }

        Map<String, ?> oldData;

        if (tableName.equalsIgnoreCase("goods")) {
            oldData = jsonDataManager.read(fileName, Good.class);
        } else {
            oldData = jsonDataManager.read(fileName, Shop.class);
        }

        if (oldData.containsKey(String.valueOf(id))) {
            throw new IllegalArgumentException("ID already exists: " + id);
        }

        Map<String, Object> objectData = new HashMap<>(data);
        objectData.remove("id");

        Object object;

        if (tableName.equalsIgnoreCase("goods")) {
            object = jsonDataManager.convertToObject(objectData, Good.class);
        } else {
            object = jsonDataManager.convertToObject(objectData, Shop.class);
        }

        Map<String, Object> result = new HashMap<>(oldData);

        result.put(String.valueOf(id), object);

        jsonDataManager.write(fileName, result);
    }


    public void update(String tableName, Map<String, Object> data) {

    }

    public void delete(String tableName, List<List<Condition>> where) throws IOException {

        String fileName = "data/" + tableName.toLowerCase() + ".json";

        Map<String, ?> oldData;

        JsonDataManager jsonDataManager = new JsonDataManager();

        if (tableName.equalsIgnoreCase("goods")) {
            oldData = jsonDataManager.read(fileName, Good.class);
        } else if (tableName.equalsIgnoreCase("shops")) {
            oldData = jsonDataManager.read(fileName, Shop.class);
        } else {
            throw new IllegalArgumentException("Unknown table: " + tableName);
        }

        List<String> idsToDelete = new ArrayList<>();

        for (Map.Entry<String, ?> entry : oldData.entrySet()) {

            Object object = entry.getValue();

            Integer id = Integer.valueOf(entry.getKey());

            Map<String, Object> row = jsonDataManager.convertToMap(object);

            row.put("id", id);

            if (Condition.matches(row, where)) {
                idsToDelete.add(entry.getKey());
            }
        }

        for (String id : idsToDelete) {
            oldData.remove(id);
        }

        jsonDataManager.write(fileName, oldData);
    }

    public List<String> getColumns(String tableName) {

        if (tableName.equalsIgnoreCase("goods")) {
            return List.of("id", "name", "category", "price", "shopId");
        }
        if (tableName.equalsIgnoreCase("shops")) {
            return List.of("id", "address", "city", "openingDate", "isOpen24h");
        }
        throw new IllegalArgumentException("Unknown table: " + tableName);
    }
}
