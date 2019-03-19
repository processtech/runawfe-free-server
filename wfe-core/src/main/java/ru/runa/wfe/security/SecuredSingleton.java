package ru.runa.wfe.security;

import org.springframework.util.Assert;

/**
 * Collection of SecuredObject singletons which have identifiableId=0. E.g. former ASystem is here: SecuredSingleton.SYSTEM.
 * Static instance names are the same as corresponding SecuredObjectType "enum item" names.
 * <p>
 * These singletons are to simplify code by avoiding overloaded methods: for SecuredObject and SecuredObjectType.
 * <p>
 * Inherits from SecuredObjectBase, not SecuredObject, to preserve old toString() behaviour.
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
    public static final SecuredSingleton DATAFILE = new SecuredSingleton(SecuredObjectType.DATAFILE);
    public static final SecuredSingleton DEFINITIONS = new SecuredSingleton(SecuredObjectType.DEFINITIONS);
    public static final SecuredSingleton ERRORS = new SecuredSingleton(SecuredObjectType.ERRORS);
    public static final SecuredSingleton EXECUTORS = new SecuredSingleton(SecuredObjectType.EXECUTORS);
    public static final SecuredSingleton LOGS = new SecuredSingleton(SecuredObjectType.LOGS);
    public static final SecuredSingleton PROCESSES = new SecuredSingleton(SecuredObjectType.PROCESSES);
    public static final SecuredSingleton RELATIONS = new SecuredSingleton(SecuredObjectType.RELATIONS);
    public static final SecuredSingleton REPORTS = new SecuredSingleton(SecuredObjectType.REPORTS);
    public static final SecuredSingleton SCRIPTS = new SecuredSingleton(SecuredObjectType.SCRIPTS);
    public static final SecuredSingleton SUBSTITUTION_CRITERIAS = new SecuredSingleton(SecuredObjectType.SUBSTITUTION_CRITERIAS);
    public static final SecuredSingleton SYSTEM = new SecuredSingleton(SecuredObjectType.SYSTEM);
}
