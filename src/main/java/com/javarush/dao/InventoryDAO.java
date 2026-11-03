package com.javarush.dao;

import com.javarush.domain.Inventory;
import org.hibernate.SessionFactory;

public class InventoryDAO extends GenericDAO<Inventory> {
    public InventoryDAO(SessionFactory sessionFactory) {
        super(Inventory.class, sessionFactory);
    }

    /**
     * Экземпляр (inventory), доступный для аренды: по нему либо вообще нет записей в rental,
     * либо у всех его аренд заполнен return_date (т.е. и у последней тоже).
     */
    public Inventory getFirstAvailableForRent() {
        return getCurrentSession()
                .createQuery("select i from Inventory i " +
                        "where not exists (" +
                        "    select r.id from Rental r where r.inventory = i and r.returnDate is null" +
                        ") order by i.id", Inventory.class)
                .setMaxResults(1)
                .uniqueResult();
    }
}
