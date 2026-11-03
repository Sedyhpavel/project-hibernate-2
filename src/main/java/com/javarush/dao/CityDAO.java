package com.javarush.dao;

import com.javarush.domain.City;
import org.hibernate.SessionFactory;

public class CityDAO extends GenericDAO<City> {
    public CityDAO(SessionFactory sessionFactory) {
        super(City.class, sessionFactory);
    }

    /** Названия городов в БД не уникальны (ещё один «ляп»), поэтому берём первый найденный. */
    public City getByName(String name) {
        return getCurrentSession()
                .createQuery("select c from City c where c.city = :name", City.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .uniqueResult();
    }
}
