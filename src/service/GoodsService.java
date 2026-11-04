package service;

import model.Shop;

public class GoodsService {

    public String select(String columns, String where){
        return "[заглушка] SELECT goods: columns=" + columns + ", where=" + where;
    }

    public String insert(String columns, String values){
        return "[заглушка] INSERT goods: columns=" + columns + ", values=" + values;
    }

    public String update(String set, String where){
        return "[заглушка] UPDATE goods: set=" + set + ", where=" + where;
    }

    public String delete(String where){
        return "[заглушка] DELETE goods: where=" + where;
    }
}
