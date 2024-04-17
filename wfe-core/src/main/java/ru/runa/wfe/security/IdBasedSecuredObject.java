package ru.runa.wfe.security;

public abstract class IdBasedSecuredObject extends SecuredObject {
    private static final long serialVersionUID = 1L;

    public abstract Long getId();

    @Override
    public Long getSecuredObjectId() {
        return getId();
    }

}
