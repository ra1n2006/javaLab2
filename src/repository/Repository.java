package repository;

import model.Good;
import model.Shop;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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

    public void insert(String tableName, Map<String, Object> cl) {

    }

    public void update(String tableName, Map<String, Object> cl) {

    }

    public void delete(String tableName, Condition condition){

    }

    public List<String> getColumns(String tableName){
        return null;
    }
}
