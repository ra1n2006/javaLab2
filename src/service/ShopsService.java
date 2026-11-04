package service;

public class ShopsService {
    public String select(String columns, String where){
        return "[заглушка] SELECT shops: columns=" + columns + ", where=" + where;
    }

    public String insert(String columns, String values){
        return "[заглушка] INSERT shops: columns=" + columns + ", values=" + values;
    }

    public String update(String set, String where){
        return "[заглушка] UPDATE shops: set=" + set + ", where=" + where;
    }

    public String delete(String where){
        return "[заглушка] DELETE shops: where=" + where;
    }
}
