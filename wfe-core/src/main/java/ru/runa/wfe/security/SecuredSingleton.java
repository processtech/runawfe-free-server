/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
public final class SecuredSingleton extends SecuredObjectBase {
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
    public static final SecuredSingleton DEFINITIONS = new SecuredSingleton(SecuredObjectType.DEFINITIONS);
    public static final SecuredSingleton ERRORS = new SecuredSingleton(SecuredObjectType.ERRORS);
    public static final SecuredSingleton RELATIONS = new SecuredSingleton(SecuredObjectType.RELATIONS);
    public static final SecuredSingleton REPORTS = new SecuredSingleton(SecuredObjectType.REPORTS);
    public static final SecuredSingleton SYSTEM = new SecuredSingleton(SecuredObjectType.SYSTEM);
}
