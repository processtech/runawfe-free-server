package ru.runa.wfe.office.doc;

import com.google.common.base.Objects;

public abstract class Operation {

    public String getName() {
        return getClass().getSimpleName();
    }

    public abstract boolean isValid();

    public boolean isEndBlock(String paragraphText) {
        return (DocxUtils.CLOSING_PLACEHOLDER_START + getName() + DocxUtils.PLACEHOLDER_END).equals(paragraphText.trim());
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", getName()).toString();
    }
}
