package ru.runa.wfe.commons;

public interface ITypeConvertor {

    <T> T convertTo(Object object, Class<T> classConvertTo);
}
