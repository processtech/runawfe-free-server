package ru.runa.wfe.report;

import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

public class ReportsSecure extends SecuredObjectBase {

    private static final long serialVersionUID = 1L;

    public static final ReportsSecure INSTANCE = new ReportsSecure();

    private ReportsSecure() {
    }

    @Override
    public Long getId() {
        return 0L;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.REPORT;
    }
}
