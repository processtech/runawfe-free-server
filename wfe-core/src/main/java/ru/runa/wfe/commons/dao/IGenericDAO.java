package ru.runa.wfe.commons.dao;

public interface IGenericDAO<T> {

    /**
     * Load entity from database by id.
     * 
     * @return entity or <code>null</code> if no entity found.
     */
    public T get(Long id);

}
