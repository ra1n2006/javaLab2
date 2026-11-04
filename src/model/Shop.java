package model;

import java.time.LocalDate;

/**
 * Магазин — запись таблицы shops.
 * На магазин ссылаются товары через поле Good.shopId.
 */
public class Shop {
    /**
     * Уникальный идентификатор магазина (первичный ключ).
     */
    private Integer id;

    /**
     * Адрес магазина.
     */
    private String address;

    /**
     * Город, в котором находится магазин.
     */
    private String city;

    /**
     * Дата открытия магазина.
     */
    private LocalDate openingDate;

    /**
     * Работает ли магазин круглосуточно.
     */
    private Boolean isOpen24h;

    /**
     * Создаёт пустой магазин. Нужен для загрузки из JSON.
     */
    public Shop() {
    }

    /**
     * Создаёт магазин со всеми полями.
     *
     * @param id          идентификатор магазина
     * @param address     адрес
     * @param city        город
     * @param openingDate дата открытия
     * @param isOpen24h   работает ли круглосуточно
     */
    public Shop(Integer id, String address, String city, LocalDate openingDate, Boolean isOpen24h) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.openingDate = openingDate;
        this.isOpen24h = isOpen24h;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public Boolean getIsOpen24h() {
        return isOpen24h;
    }

    public void setIsOpen24h(Boolean isOpen24h) {
        this.isOpen24h = isOpen24h;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", openingDate=" + openingDate +
                ", isOpen24h=" + isOpen24h +
                '}';
    }
}
