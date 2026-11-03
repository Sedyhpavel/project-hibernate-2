package com.javarush;

import com.javarush.dao.*;
import com.javarush.domain.*;
import com.p6spy.engine.spy.P6SpyDriver;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

public class Main {
    private final SessionFactory sessionFactory;

    private final ActorDAO actorDAO;
    private final AddressDAO addressDAO;
    private final CategoryDAO categoryDAO;
    private final CityDAO cityDAO;
    private final CountryDAO countryDAO;
    private final CustomerDAO customerDAO;
    private final FilmDAO filmDAO;
    private final FilmTextDAO filmTextDAO;
    private final InventoryDAO inventoryDAO;
    private final LanguageDAO languageDAO;
    private final PaymentDAO paymentDAO;
    private final RentalDAO rentalDAO;
    private final StaffDAO staffDAO;
    private final StoreDAO storeDAO;

    public Main() {
        Properties properties = new Properties();
        // p6spy-драйвер оборачивает настоящий MySQL-драйвер и логирует SQL с подставленными параметрами
        properties.put(Environment.DRIVER, P6SpyDriver.class.getName());
        properties.put(Environment.URL, System.getProperty("db.url", "jdbc:p6spy:mysql://localhost:3306/movie"));
        properties.put(Environment.USER, System.getProperty("db.user", "root"));
        properties.put(Environment.PASS, System.getProperty("db.password", "123456789asdfghjkl;"));
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
        // Схему НЕ трогаем: структуру таблиц менять нельзя
        properties.put(Environment.HBM2DDL_AUTO, "none");

        sessionFactory = new Configuration()
                .addAnnotatedClass(Actor.class)
                .addAnnotatedClass(Address.class)
                .addAnnotatedClass(Category.class)
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(Customer.class)
                .addAnnotatedClass(Film.class)
                .addAnnotatedClass(FilmText.class)
                .addAnnotatedClass(Inventory.class)
                .addAnnotatedClass(Language.class)
                .addAnnotatedClass(Payment.class)
                .addAnnotatedClass(Rental.class)
                .addAnnotatedClass(Staff.class)
                .addAnnotatedClass(Store.class)
                .addProperties(properties)
                .buildSessionFactory();

        actorDAO = new ActorDAO(sessionFactory);
        addressDAO = new AddressDAO(sessionFactory);
        categoryDAO = new CategoryDAO(sessionFactory);
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        customerDAO = new CustomerDAO(sessionFactory);
        filmDAO = new FilmDAO(sessionFactory);
        filmTextDAO = new FilmTextDAO(sessionFactory);
        inventoryDAO = new InventoryDAO(sessionFactory);
        languageDAO = new LanguageDAO(sessionFactory);
        paymentDAO = new PaymentDAO(sessionFactory);
        rentalDAO = new RentalDAO(sessionFactory);
        staffDAO = new StaffDAO(sessionFactory);
        storeDAO = new StoreDAO(sessionFactory);
    }

    public static void main(String[] args) {
        Main main = new Main();
        try {
            Customer customer = main.createCustomer();
            System.out.println(">>> Создан покупатель id=" + customer.getId());

            Rental returned = main.customerReturnInventoryToStore();
            System.out.println(">>> Возвращена аренда id=" + returned.getId());

            Rental rental = main.customerRentInventory(customer);
            System.out.println(">>> Новая аренда id=" + rental.getId());

            Film film = main.newFilmWasMade();
            System.out.println(">>> Новый фильм id=" + film.getId() + " «" + film.getTitle() + "»");
        } finally {
            main.sessionFactory.close();
        }
    }

    /**
     * Обёртка «одна операция = одна транзакция»: при любой ошибке делаем rollback,
     * чтобы не получилось, например, что адрес записан, а покупатель – нет.
     */
    private <R> R inTransaction(Function<Session, R> action) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try {
            R result = action.apply(session);
            transaction.commit();
            return result;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    /** 1. Новый покупатель со всеми зависимыми данными (адрес). */
    public Customer createCustomer() {
        return inTransaction(session -> {
            Store store = storeDAO.getFirst();

            City city = cityDAO.getByName("Kursk");
            if (city == null) {
                city = cityDAO.getFirst();
            }

            Address address = new Address();
            address.setAddress("Lenina st, 1");
            address.setDistrict("Kursk oblast");
            address.setCity(city);
            address.setPostalCode("305000");
            address.setPhone("+79990001122");
            addressDAO.save(address);

            Customer customer = new Customer();
            customer.setStore(store);
            customer.setFirstName("IVAN");
            customer.setLastName("PETROV");
            customer.setEmail("ivan.petrov@example.com");
            customer.setAddress(address);
            customer.setActive(true);
            customerDAO.save(customer);
            return customer;
        });
    }

    /** 2. Покупатель вернул ранее арендованный фильм. */
    public Rental customerReturnInventoryToStore() {
        return inTransaction(session -> {
            Rental rental = rentalDAO.getAnyUnreturnedRental();
            if (rental == null) {
                throw new IllegalStateException("Нет ни одной незакрытой аренды");
            }
            rental.setReturnDate(LocalDateTime.now());
            rentalDAO.update(rental);
            return rental;
        });
    }

    /** 3. Покупатель пришёл в магазин, арендовал доступный экземпляр и оплатил продавцу. */
    public Rental customerRentInventory(Customer customer) {
        return inTransaction(session -> {
            Inventory inventory = inventoryDAO.getFirstAvailableForRent();
            if (inventory == null) {
                throw new IllegalStateException("Нет доступных для аренды экземпляров");
            }
            Store store = inventory.getStore();
            Staff staff = store.getManager();

            Rental rental = new Rental();
            rental.setCustomer(customer);
            rental.setInventory(inventory);
            rental.setStaff(staff);
            rental.setRentalDate(LocalDateTime.now());
            rentalDAO.save(rental);

            Payment payment = new Payment();
            payment.setCustomer(customer);
            payment.setStaff(staff);
            payment.setRental(rental);
            payment.setAmount(inventory.getFilm().getRentalRate());
            paymentDAO.save(payment);
            return rental;
        });
    }

    /** 4. Сняли новый фильм, и он стал доступен для аренды (появился в инвентаре магазинов). */
    public Film newFilmWasMade() {
        return inTransaction(session -> {
            Language language = languageDAO.getFirst();
            List<Category> categories = categoryDAO.getItems(0, 2);
            List<Actor> actors = actorDAO.getItems(0, 5);

            Film film = new Film();
            film.setTitle("JAVARUSH HIBERNATE ADVENTURE");
            film.setDescription("A Thoughtful Story of a Java Developer Who Must Map a Legacy Database");
            film.setReleaseYear(2026);
            film.setLanguage(language);
            film.setRentalDuration((short) 5);
            film.setRentalRate(new BigDecimal("2.99"));
            film.setLength(120);
            film.setReplacementCost(new BigDecimal("19.99"));
            film.setRating(Rating.PG13);
            film.setSpecialFeatures(EnumSet.of(Feature.TRAILERS, Feature.BEHIND_THE_SCENES));
            film.setActors(new HashSet<>(actors));
            film.setCategories(new HashSet<>(categories));
            filmDAO.save(film);
            session.flush();

            // В оригинальной sakila film_text заполняет триггер ins_film, в дампе JavaRush его может не быть.
            // Поэтому сначала проверяем, есть ли уже строка, и только потом создаём/обновляем.
            FilmText filmText = filmTextDAO.getById(film.getId());
            if (filmText == null) {
                filmText = new FilmText();
                filmText.setFilm(film);
                filmText.setTitle(film.getTitle());
                filmText.setDescription(film.getDescription());
                filmTextDAO.save(filmText);
            } else {
                filmText.setTitle(film.getTitle());
                filmText.setDescription(film.getDescription());
            }
            film.setFilmText(filmText);

            for (Store store : storeDAO.findAll()) {
                Inventory inventory = new Inventory();
                inventory.setFilm(film);
                inventory.setStore(store);
                inventoryDAO.save(inventory);
            }
            return film;
        });
    }
}
