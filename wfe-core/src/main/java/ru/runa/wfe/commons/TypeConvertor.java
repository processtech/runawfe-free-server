package ru.runa.wfe.commons;

public interface TypeConvertor {

    <T> T convertTo(Object object, Class<T> classConvertTo);
}
