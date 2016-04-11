package ru.runa.wfe.service.jaxb;

import com.google.common.base.Objects;

public class Variable {
    public String name;
    public String scriptingName;
    public String format;
    public String value;

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).add("value", value).toString();
    }
}
