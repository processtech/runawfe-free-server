package ru.runa.wfe.commons.dao;

/**
 * Common API for normal DAOs (subclasses of GenericDao) and synthetic DAOs (subclasses of GenericDao2).
 */
public interface GenericDaoApi<T> {

    T get(Long id);
    T getNotNull(Long id);
}
