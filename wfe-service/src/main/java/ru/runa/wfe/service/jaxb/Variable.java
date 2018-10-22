package ru.runa.wfe.service.jaxb;

import com.google.common.base.MoreObjects;

public class Variable {
    public String name;
    public String scriptingName;
    public String format;
    public String value;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name).add("value", value).toString();
    }
}
