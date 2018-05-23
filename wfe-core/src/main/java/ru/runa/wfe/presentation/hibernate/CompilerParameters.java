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
package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;
import java.util.List;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.task.Task;

/**
 * Parameter object. Parameters, used to build HQL/SQL query.
 */
public class CompilerParameters {

    /**
     * Restrictions for loaded objects owner's. For example we want to load only our tasks or only tasks by other user.
     */
    private final RestrictionsToOwners ownersRestrictions;

    /**
     * Restrictions to load only objects with specified permission granted for user.
     */
    private final RestrictionsToPermissions permissionRestrictions;

    /**
     * Flag, equals true, if paging must be used in query; false otherwise.
     */
    private final boolean enablePaging;

    /**
     * Flag, equals true, if only objects count must be queried.
     */
    private final boolean isCountQuery;

    /**
     * Subclass of root persisted object, to be queried. May be null, if root persistent object and all it's subclasses must be queried.
     */
    private final Class<?> requestedClass;

    /**
     * Restrictions, applied to object identity. Must be HQL query string or null. If set, added to query in form 'object id in (idRestriction)'.
     */
    private final String[] idRestriction;

    /**
     * Flag, equals true, if only identity of objects must be loaded; false to load entire object. <br/>
     * <b>Loaded object must have id property.</b>
     */
    private final boolean onlyIdentityLoad;

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, except isCountQuery flag.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     * @param isCountQuery
     *            Flag, equals true, if only objects count must be queried.
     */
    CompilerParameters(CompilerParameters src, boolean isCountQuery) {
        if (src == null) {
            throw new InternalApplicationException("Parameter for hibernate compiler must be set.");
        }
        this.ownersRestrictions = src.ownersRestrictions;
        this.enablePaging = isCountQuery ? false : src.enablePaging;
        this.isCountQuery = isCountQuery;
        this.permissionRestrictions = src.permissionRestrictions;
        this.requestedClass = src.requestedClass;
        this.idRestriction = src.idRestriction;
        this.onlyIdentityLoad = src.onlyIdentityLoad;
    }

    /**
     * Creates parameter object for building HQL query without any additional check.
     * 
     * @param enablePaging
     *            Flag, equals true, if paging must be used in request; false otherwise.
     */
    private CompilerParameters(boolean enablePaging) {
        this.ownersRestrictions = null;
        this.enablePaging = enablePaging;
        this.isCountQuery = false;
        this.permissionRestrictions = null;
        this.requestedClass = null;
        this.idRestriction = null;
        this.onlyIdentityLoad = false;
    }

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, and set owners restrictions.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     * @param owners
     *            Restrictions for loaded objects owner's. For example we want to load only our tasks or only tasks by other user.
     */
    private CompilerParameters(CompilerParameters src, RestrictionsToOwners owners) {
        this.ownersRestrictions = owners;
        this.enablePaging = src.enablePaging;
        this.isCountQuery = src.isCountQuery;
        this.permissionRestrictions = src.permissionRestrictions;
        this.requestedClass = src.requestedClass;
        this.idRestriction = src.idRestriction;
        this.onlyIdentityLoad = src.onlyIdentityLoad;
    }

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, and set permissions restrictions.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     * @param permissions
     *            Restrictions to load only objects with specified permission granted for user.
     */
    private CompilerParameters(CompilerParameters src, RestrictionsToPermissions permissions) {
        this.ownersRestrictions = src.ownersRestrictions;
        this.enablePaging = src.enablePaging;
        this.isCountQuery = src.isCountQuery;
        this.permissionRestrictions = permissions;
        this.requestedClass = src.requestedClass;
        this.idRestriction = src.idRestriction;
        this.onlyIdentityLoad = src.onlyIdentityLoad;
    }

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, and set permissions restrictions.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     * @param requestedClass
     *            Subclass of root persisted object, to be queried.
     */
    private CompilerParameters(CompilerParameters src, Class<?> requestedClass) {
        this.ownersRestrictions = src.ownersRestrictions;
        this.enablePaging = src.enablePaging;
        this.isCountQuery = src.isCountQuery;
        this.permissionRestrictions = src.permissionRestrictions;
        this.requestedClass = requestedClass;
        this.idRestriction = src.idRestriction;
        this.onlyIdentityLoad = src.onlyIdentityLoad;
    }

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, and set only identity flag.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     */
    private CompilerParameters(CompilerParameters src, OnlyIdentity dummy) {
        this.ownersRestrictions = src.ownersRestrictions;
        this.enablePaging = src.enablePaging;
        this.isCountQuery = src.isCountQuery;
        this.permissionRestrictions = src.permissionRestrictions;
        this.requestedClass = src.requestedClass;
        this.idRestriction = src.idRestriction;
        this.onlyIdentityLoad = true;
    }

    /**
     * Creates parameter object for building HQL query using other {@linkplain CompilerParameters} as source. Copy all parameters from source
     * {@linkplain CompilerParameters}, and set only identity flag.
     * 
     * @param src
     *            {@linkplain CompilerParameters} to copy parameters from.
     * @param idRestriction
     *            Restrictions, applied to object identity. Must be HQL query string or null.
     */
    private CompilerParameters(CompilerParameters src, IdRestriction idRestriction) {
        this.ownersRestrictions = src.ownersRestrictions;
        this.enablePaging = src.enablePaging;
        this.isCountQuery = src.isCountQuery;
        this.permissionRestrictions = src.permissionRestrictions;
        this.requestedClass = src.requestedClass;
        this.idRestriction = idRestriction.idRestriction;
        this.onlyIdentityLoad = src.onlyIdentityLoad;
    }

    /**
     * Check, if HQL/SQL query must return only objects count.
     * 
     * @return true, if HQL/SQL query must return only objects count; false, if list of objects must be returned.
     */
    public boolean isCountQuery() {
        return isCountQuery;
    }

    /**
     * Check, if paging must be used in request.
     * 
     * @return true, if paging must be used in request; false otherwise.
     */
    public boolean isPagingEnabled() {
        return enablePaging;
    }

    /**
     * Check, if query must have owners restrictions.
     * 
     * @return true, if query must have owners restrictions and false otherwise.
     */
    public boolean hasOwners() {
        return ownersRestrictions != null;
    }

    /**
     * Subclass of root persisted object, to be queried. May be null, if root persistent object and all it's subclasses must be queried.
     * 
     * @return Subclass of root persisted object, to be queried.
     */
    public Class<?> getQueriedClass() {
        return requestedClass;
    }

    /**
     * Collection of owners id (Long for example).
     * 
     * @return Collection of owners id.
     */
    public Collection<?> getOwners() {
        return ownersRestrictions == null ? null : ownersRestrictions.getOwners();
    }

    /**
     * HQL path from root object to calculate object owner (actorId for {@link Task} for example).
     * 
     * @return HQL path from root object to calculate object owner.
     */
    public String getOwnerDBPath() {
        return ownersRestrictions == null ? null : ownersRestrictions.getOwnersDBPath();
    }

    public RestrictionsToPermissions getPermissionRestrictions() {
        return permissionRestrictions;
    }

    /**
     * Restrictions, applied to object identity. Must be HQL query string or null. If set, added to query in form 'object id in (idRestriction)'.
     * 
     * @return Return array of restrictions, applied to object identity.
     */
    public String[] getIdRestriction() {
        return idRestriction;
    }

    /**
     * Flag, equals true, if only identity of objects must be loaded; false to load entire object. <br/>
     * <b>Loaded object must have id property.</b>
     * 
     * @return true, if only identity must be loaded.
     */
    public boolean isOnlyIdentityLoad() {
        return onlyIdentityLoad;
    }

    /**
     * Creates compiler parameters for simple object's loading without any restrictions.
     * 
     * @param enablePaging
     *            Flag, equals true if page loading enabled and false otherwise.
     * @return Returns batch presentation compiler parameters.
     */
    public static CompilerParameters create(boolean enablePaging) {
        return new CompilerParameters(enablePaging);
    }

    /**
     * Creates compiler parameters for simple object's loading without any restrictions. Objects loaded with paging support.
     * 
     * @return Returns batch presentation compiler parameters.
     */
    public static CompilerParameters createPaged() {
        return new CompilerParameters(true);
    }

    /**
     * Creates compiler parameters for simple object's loading without any restrictions. Objects loaded without paging support (all objects will be
     * loaded).
     * 
     * @return Returns batch presentation compiler parameters.
     */
    public static CompilerParameters createNonPaged() {
        return new CompilerParameters(false);
    }

    /**
     * Creates compiler parameters some as current and add owners restriction.
     * 
     * @param owners
     *            Restrictions for loaded objects owner's. For example we want to load only our tasks or only tasks by other user.
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters addOwners(RestrictionsToOwners owners) {
        return new CompilerParameters(this, owners);
    }

    /**
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters addPermissions(RestrictionsToPermissions permissions) {
        return new CompilerParameters(this, permissions);
    }

    /**
     * Creates compiler parameters some as current and add .
     * 
     * @param requestedClass
     *            Subclass of root persisted object, to be queried.
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters addRequestedClass(Class<?> requestedClass) {
        return new CompilerParameters(this, requestedClass);
    }

    /**
     * Creates compiler parameters some as current and add .
     * 
     * @param idRestrictions
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters addIdRestrictions(String[] idRestrictions) {
        return new CompilerParameters(this, new IdRestriction(idRestrictions));
    }

    /**
     * Creates compiler parameters some as current and add .
     * 
     * @param idRestriction
     *            Restrictions, applied to object identity. Must be HQL query string or null.
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters addIdRestrictions(String idRestriction) {
        return new CompilerParameters(this, new IdRestriction(new String[] { idRestriction }));
    }

    /**
     * Creates compiler parameters some as current and add condition to load only identity of objects.
     * 
     * @return Returns batch presentation compiler parameters.
     */
    public CompilerParameters loadOnlyIdentity() {
        return new CompilerParameters(this, new OnlyIdentity());
    }

    /**
     * Dummy class for only identity parameter fluent interface.
     */
    private static class OnlyIdentity {

    }

    private static class IdRestriction {
        final String[] idRestriction;

        IdRestriction(String[] idRestrictions) {
            this.idRestriction = idRestrictions;
        }
    }
}
