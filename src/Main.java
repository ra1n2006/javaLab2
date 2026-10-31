import java.util.Scanner;

class CommandLineInterface {

    private final Scanner scanner;
    //private final ShopsService shopsService;
    //private final GoodsService goodsService;


    public CommandLineInterface() {
        this.scanner = new Scanner(System.in);
        //this.shopsService = new ShopsService();
        //this.goodsService = new GoodsService();
    }

    /* Далее здесь должен быть метод, запускающий ввод данных до тех пор,
    пока не будет введена команда о выходе.

    Текущая команда записывается в отдельную строковую переменную

    Затем определяется, какой это именно SQL-запрос (лучше всего регулярным выражением)
    Из строки запроса нужно обязательно выделить таблицу и в зависимости от этого вызвать соответствующий сервис
    например: SELECT * FROM goods
    вызываешь goodsService.select("*")
    аналогично например:
    SELECT id FROM shops -> shopsService.select("id")
    INSERT INTO shops VALUES (1, "Киров", "Ленина 5") -> shopsService.insert("1, Киров, Ленина 5")
     */
}


public class Main {
    public static void main(String[] args) {

    }
}