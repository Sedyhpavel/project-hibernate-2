# Hibernate: проект №2 (модуль 4 JavaRush) — маппинг схемы `movie`

Maven-проект с энтити для всех таблиц схемы `movie` (это MySQL-овская тестовая БД *sakila*)
и четырьмя транзакционными методами, которые проверяют, что маппинг сделан правильно.

## Как запустить

1. Разверни дамп `movie` в локальном MySQL.
2. Проверь логин/пароль: по умолчанию `root` / `root`, URL `jdbc:p6spy:mysql://localhost:3306/movie`.
   Переопределить можно VM-опциями: `-Ddb.user=... -Ddb.password=... -Ddb.url=...`.
3. Запусти `com.javarush.Main` (или `mvn compile exec:java`).
   В консоль p6spy выведет все SQL-запросы с подставленными параметрами.

Стек: `hibernate-core-jakarta 5.6.15`, `mysql-connector-java 8.0.33`, `p6spy 3.9.1`, Java 17+.

## Что сделано

| Метод в `Main` | Что делает |
|---|---|
| `createCustomer()` | создаёт адрес и покупателя в одной транзакции |
| `customerReturnInventoryToStore()` | находит незакрытую аренду и проставляет `return_date` |
| `customerRentInventory(customer)` | находит доступный экземпляр (нет открытой аренды), создаёт `rental` и `payment` у менеджера магазина |
| `newFilmWasMade()` | создаёт фильм с актёрами и категориями, `film_text` и по экземпляру в каждом магазине |

Все методы идут через `inTransaction(...)`: commit при успехе, rollback при любом исключении.

Связи:
- `film` → `language`, `original_language` — ManyToOne
- `film` ↔ `actor` (через `film_actor`), `film` ↔ `category` (через `film_category`) — ManyToMany
- `film_text` → `film` — OneToOne с общим ключом (`@MapsId`)
- `rental` → `inventory`, `customer`, `staff`; `payment` → `customer`, `staff`, `rental`
- `inventory` → `film`, `store`; `store` → `manager (staff)`, `address`
- `customer`, `staff` → `address` → `city` → `country`

`rating` (ENUM) и `special_features` (SET) замаплены через свои enum'ы `Rating` и `Feature`.

## Найденные «ляпы» в структуре БД

1. **`film_text.film_id` без foreign key на `film.film_id`.** Можно получить «текст» несуществующего
   фильма. Нужен FK.
2. **`film_text` дублирует `film.title` и `film.description`.** Это денормализация: данные хранятся
   дважды и могут разъехаться. В оригинальной sakila её поддерживают триггеры, но правильнее
   вообще убрать таблицу (её единственный смысл — FULLTEXT-индекс) и сделать индекс на `film`.
3. **`film.original_language_id` — во всех строках `NULL`.** Мёртвая колонка: либо заполнять, либо удалить.
4. **Цикл `store` ↔ `staff`.** `store.manager_staff_id` → `staff`, а `staff.store_id` → `store`.
   Первый магазин и первого сотрудника нельзя добавить без отключения проверок FK.
   Лучше вынести «менеджер магазина» в отдельную связь или флаг/роль в `staff`.
5. **Дублирование в `payment`.** `payment.customer_id` повторяет `rental.customer_id`, а
   `payment.staff_id` может не совпадать с `rental.staff_id`. Есть риск противоречивых данных.
6. **`payment.rental_id` допускает `NULL`.** Бывают платежи без аренды; по схеме непонятно, за что они.
7. **Нет ограничения «один экземпляр — одна открытая аренда».** Один `inventory` можно выдать
   дважды одновременно (две строки `rental` с `return_date IS NULL`). Проверка есть только в коде.
8. **Слишком маленькие типы ключей.** `TINYINT UNSIGNED` для магазинов, сотрудников, языков,
   категорий — максимум 255 записей; `SMALLINT UNSIGNED` для покупателей, фильмов, адресов,
   платежей — максимум 65 535. Для `payment` (16 тыс. строк уже сейчас) это реальный предел.
9. **`release_year` типа `YEAR`.** Допустимый диапазон только 1901–2155, старые фильмы не внести.
10. **ENUM (`rating`) и SET (`special_features`).** Не переносимы между СУБД, а добавить новое значение
    можно только через `ALTER TABLE`. Лучше справочные таблицы.
11. **Нет уникальности, где она логически нужна.** `customer.email`, `staff.email`,
    пары `city` + `country` не уникальны (в `city` встречаются повторяющиеся названия).
12. **`staff.password VARCHAR(40)`.** Рассчитан на SHA-1 без соли — слабое хеширование паролей.
    Нужен bcrypt/argon2 и поле подлиннее.
13. **`staff.picture BLOB` в основной таблице.** Бинарные данные лучше хранить отдельно
    (или ссылку на файл), чтобы не тянуть их при каждом чтении сотрудника.
14. **Телефон хранится в `address`.** Телефон — свойство человека, а не адреса; при общем
    адресе у двух людей будет один телефон.
15. **`language.name CHAR(20)`.** Фиксированная длина с добивкой пробелами; лучше `VARCHAR`.
