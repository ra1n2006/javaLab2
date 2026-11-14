package repository;

import model.Table;

import java.io.IOException;
import java.util.List;

public interface TableRepository {

    void createTable(Table table) throws IOException;

    Table getTable(String tableName) throws IOException;

    void deleteTable(String tableName);

    void writeTable(Table table) throws IOException;

    List<Table> listTables() throws IOException;

    boolean exists(String tableName);
}