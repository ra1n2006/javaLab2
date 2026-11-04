package model;

/**
 * Товар — запись таблицы goods.
 * Каждый товар хранится в одном магазине, связь через {@link #shopId}.
 */
public class Good {

    /**
     * Уникальный идентификатор товара (первичный ключ).
     */
    private Integer id;

    /**
     * Наименование товара.
     */
    private String name;

    /**
     * Категория товара.
     */
    private String category;

    /**
     * Цена за единицу товара.
     */
    private Double price;

    /**
     * Идентификатор магазина, в котором хранится товар (внешний ключ на Shop.id).
     */
    private Integer shopId;

    /**
     * Создаёт пустой товар. Нужен для загрузки из JSON.
     */
    public Good(){}

    /**
     * Создаёт товар со всеми полями.
     *
     * @param id       идентификатор товара
     * @param name     наименование
     * @param category категория
     * @param price    цена за единицу
     * @param shopId   идентификатор магазина
     */
    public Good(Integer id, String name, String category, Double price, Integer shopId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.shopId = shopId;

    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    @Override
    public String toString() {
        return "Good{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", shopId=" + shopId +
                '}';
    }
}