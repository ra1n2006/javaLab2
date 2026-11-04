# javaLab2

## Инструкция пользователя

### Запуск

Открыть проект в IntelliJ IDEA и запустить класс `Main`.

Или из терминала, в корне проекта:

```
javac -d out src/Main.java src/service/*.java src/model/*.java src/repository/*.java
java -cp out Main
```

### Формат ввода

- Каждый запрос заканчивается точкой с запятой `;`.
- Запрос можно вводить в несколько строк: пока нет `;`, программа ждёт продолжения (`->`).
- Регистр команд не важен: `SELECT` и `select` одинаковы.
- `HELP;` — список команд, `EXIT;` — выход.

### Примеры команд

```sql
SELECT * FROM goods;
SELECT name, price FROM goods WHERE price > 100;
INSERT INTO goods VALUES (1, 'Банан', 50.0);
INSERT INTO shops (id, city) VALUES (2, 'Киров');
UPDATE goods SET price = 60 WHERE id = 1;
DELETE FROM goods WHERE name = 'Банан';
CREATE INDEX idx_goods_name ON goods (name);
```

При ошибке программа выводит сообщение с примером правильного запроса и продолжает работу.