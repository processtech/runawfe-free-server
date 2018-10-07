package ru.runa.wfe.commons.hibernate;

public interface DbAwareEnum<T> {

    T getDbValue();
}
