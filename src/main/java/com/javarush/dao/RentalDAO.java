package com.javarush.dao;

import com.javarush.domain.Rental;
import org.hibernate.SessionFactory;

public class RentalDAO extends GenericDAO<Rental> {
    public RentalDAO(SessionFactory sessionFactory) {
        super(Rental.class, sessionFactory);
    }

    /** Любая аренда, по которой фильм ещё не вернули. */
    public Rental getAnyUnreturnedRental() {
        return getCurrentSession()
                .createQuery("select r from Rental r where r.returnDate is null order by r.id", Rental.class)
                .setMaxResults(1)
                .uniqueResult();
    }
}
