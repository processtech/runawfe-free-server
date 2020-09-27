package ru.runa.wfe.security;

import org.springframework.util.Assert;

/**
 * Collection of SecuredObject singletons which have identifiableId=0. E.g. former ASystem is here: SecuredSingleton.SYSTEM.
 * Static instance names are the same as corresponding SecuredObjectType "enum item" names.
 * <p>
 * These singletons are to simplify code by avoiding overloaded methods: for SecuredObject and SecuredObjectType.
 * <p>
 * See also: ru.runa.wfe.service.security.SecuredObjectFactory in wfe-service module.
 *
 * @see SecuredObjectType
 */
public final class SecuredSingleton extends SecuredObject {
    private static final long serialVersionUID = 1L;
    private SecuredObjectType type;

    /**
     * Public because subprojects may extend SecuredObjectType enum, so they must be able to define corresponding singletons.
     */
    public SecuredSingleton(SecuredObjectType type) {
        Assert.isTrue(type.isSingleton());
        this.type = type;
    }

    @Override
    public final Long getId() {
        return 0L;
    }

    @Override
    public final SecuredObjectType getSecuredObjectType() {
        return type;
    }


    // Alphabetically, please:

    public static final SecuredSingleton BOTSTATIONS = new SecuredSingleton(SecuredObjectType.BOTSTATIONS);
    public static final SecuredSingleton RELATIONS = new SecuredSingleton(SecuredObjectType.RELATIONS);
    public static final SecuredSingleton REPORTS = new SecuredSingleton(SecuredObjectType.REPORTS);
    public static final SecuredSingleton SYSTEM = new SecuredSingleton(SecuredObjectType.SYSTEM);
    public static final SecuredSingleton DATASOURCES = new SecuredSingleton(SecuredObjectType.DATASOURCES);
}
