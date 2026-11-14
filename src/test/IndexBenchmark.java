
package test;

import model.Index;
import model.Table;
import repository.Repository;
import service.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IndexBenchmark {

    private static final String TABLE_NAME = "benchmark";
    private static final int ROW_COUNT = 1_000_000;
    private static final int TARGET_ID = 42424;
    private static final int ITERATIONS = 1000;

    public static void main(String[] args) {
        Repository repository = new Repository();
        Service service = new Service(repository);

        if (repository.exists(TABLE_NAME)) {
            repository.deleteTable(TABLE_NAME);
        }

        System.out.println("Создание таблицы...");

        service.createTable(TABLE_NAME, "id INT, payload STRING");

        Table table = repository.getTable(TABLE_NAME);

        for (int i = 0; i < ROW_COUNT; i++) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", i);
            row.put("payload", "Test data " + i);
            table.getRows().add(row);
        }

        repository.writeTable(table);

        System.out.println("Создано записей: " + ROW_COUNT);

        System.out.println("Создание индекса...");
        service.createIndex("idx_id", TABLE_NAME, "id");

        table = repository.getTable(TABLE_NAME);

        Index index = table.getIndexes().get("idx_id");
        List<Map<String, Object>> rows = table.getRows();

        long scanTime = 0;
        long indexTime = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            if (i % 2 == 0) {
                long start = System.nanoTime();
                scan(rows);
                scanTime += System.nanoTime() - start;

                start = System.nanoTime();
                indexedSearch(rows, index);
                indexTime += System.nanoTime() - start;
            } else {
                long start = System.nanoTime();
                indexedSearch(rows, index);
                indexTime += System.nanoTime() - start;

                start = System.nanoTime();
                scan(rows);
                scanTime += System.nanoTime() - start;
            }
        }

        double scanMs =
                scanTime / 1_000_000.0 / ITERATIONS;

        double indexMs =
                indexTime / 1_000_000.0 / ITERATIONS;

        System.out.println("\n========== BENCHMARK ==========");
        System.out.println("Количество записей: " + ROW_COUNT);
        System.out.printf("Без индекса: %.6f ms%n", scanMs);
        System.out.printf("С индексом:  %.6f ms%n", indexMs);
        System.out.printf("Ускорение:   %.2f раз%n",
                scanMs / indexMs);

        repository.deleteTable(TABLE_NAME);
    }

    private static List<Map<String, Object>> scan(
            List<Map<String, Object>> rows) {

        return rows.stream()
                .filter(row ->
                        TARGET_ID == (Integer) row.get("id"))
                .toList();
    }

    private static List<Map<String, Object>> indexedSearch(
            List<Map<String, Object>> rows,
            Index index) {

        List<Integer> indexes =
                index.getValues().get(String.valueOf(TARGET_ID));

        if (indexes == null) {
            return List.of();
        }

        return indexes.stream()
                .map(rows::get)
                .toList();
    }
}

