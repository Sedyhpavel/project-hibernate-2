package com.javarush.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.List;

/**
 * Базовый DAO. Все методы работают в рамках текущей сессии/транзакции
 * (hibernate.current_session_context_class = thread), поэтому транзакцию
 * открывает и закрывает вызывающий код – см. методы в Main.
 */
public abstract class GenericDAO<T> {
    private final Class<T> clazz;
    private final SessionFactory sessionFactory;

    protected GenericDAO(Class<T> clazz, SessionFactory sessionFactory) {
        this.clazz = clazz;
        this.sessionFactory = sessionFactory;
    }

    public T getById(Serializable id) {
        return getCurrentSession().get(clazz, id);
    }

    public List<T> getItems(int offset, int count) {
        Query<T> query = getCurrentSession().createQuery("select e from " + clazz.getName() + " e", clazz);
        query.setFirstResult(offset);
        query.setMaxResults(count);
        return query.getResultList();
    }

    public List<T> findAll() {
        return getCurrentSession().createQuery("select e from " + clazz.getName() + " e", clazz).getResultList();
    }

    public T getFirst() {
        List<T> items = getItems(0, 1);
        return items.isEmpty() ? null : items.get(0);
    }

    public T save(T entity) {
        getCurrentSession().persist(entity);
        return entity;
    }

    @SuppressWarnings("unchecked")
    public T update(T entity) {
        return (T) getCurrentSession().merge(entity);
    }

    public void delete(T entity) {
        getCurrentSession().remove(entity);
    }

    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }
}
