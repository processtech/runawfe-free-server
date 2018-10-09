package ru.runa.wfe.commons.hibernate;

/**
 * Don't delete, maybe someday we'll optimize DB row sizes.
 */
@SuppressWarnings("unused")
public interface DbAwareEnum<T> {

    T getDbValue();
}
