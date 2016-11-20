package ru.runa.wfe.presentation.filter.dialect;

public interface IDurationDialect {
    String convertOperator(String fields);

    String wrapParameter(String param);

    String convertValue(int value);
}
