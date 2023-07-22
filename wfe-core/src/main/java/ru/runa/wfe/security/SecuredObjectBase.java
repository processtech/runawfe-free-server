package ru.runa.wfe.security;

import com.google.common.base.MoreObjects;

public abstract class SecuredObjectBase extends SecuredObject {
    private static final long serialVersionUID = 1L;

    public abstract Long getId();

    @Override
    public Long getIdentifiableId() {
        return getId();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getIdentifiableId()).add("type", getSecuredObjectType()).toString();
    }
}
