package ru.runa.wfe.commons.logic;

import lombok.val;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToPermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionMapping;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Contains method to create {@linkplain PresentationConfiguredCompiler}'s, used to load object list.
 * 
 * @author Konstantinov Aleksey 27.02.2012
 */
public final class PresentationCompilerHelper {

    /**
     * Classes, loaded by queries for loading all executors (all, group children and so on).
     */
    private static final SecuredObjectType[] ALL_EXECUTORS_CLASSES = { SecuredObjectType.EXECUTOR };

    /**
     * Create {@linkplain PresentationConfiguredCompiler} for loading all executors. <b>Paging is enabled on executors loading.</b>
     * 
     * @param user
     *            Current actor.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading all executors.
     * @return {@linkplain PresentationConfiguredCompiler} for loading all executors.
     */
    public static PresentationConfiguredCompiler<Executor> createAllExecutorsCompiler(User user, BatchPresentation batchPresentation) {
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, Permission.READ, ALL_EXECUTORS_CLASSES);
        CompilerParameters parameters = CompilerParameters.createPaged().addPermissions(permissions);
        return new PresentationConfiguredCompiler<>(batchPresentation, parameters);
    }

    /**
     * Create {@linkplain PresentationConfiguredCompiler} for loading all system logs. <b>Paging is enabled on logs loading.</b>
     * 
     * @param user
     *            Current actor.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading system logs.
     * @return {@linkplain PresentationConfiguredCompiler} for loading system logs.
     */
    public static PresentationConfiguredCompiler<SystemLog> createAllSystemLogsCompiler(User user, BatchPresentation batchPresentation) {
        return new PresentationConfiguredCompiler<>(batchPresentation, CompilerParameters.createPaged());
    }

    /**
     * Create {@linkplain PresentationConfiguredCompiler} for loading group children's (or executors, which not children's for now). Only first level
     * children are loading, not recursive. <b>Paging is enabled on executors loading.</b>
     * 
     * @param user
     *            Current actor.
     * @param group
     *            {@linkplain Group}, which children's must be loaded.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading group children's.
     * @param isExclude
     *            False to load executors belonging to group and having READ permission (meaning former LIST_GROUP);
     *            true to load executors not in group and having UPDATE permission (meaning former ADD_TO_GROUP).
     *            These permissions correspond to what is checked on callers' side.
     * @return {@linkplain PresentationConfiguredCompiler} for loading group children's.
     */
    public static PresentationConfiguredCompiler<Executor> createGroupChildrenCompiler(User user, Group group, BatchPresentation batchPresentation,
            boolean isExclude) {
        String inClause = isExclude ? "NOT IN" : "IN";
        String notInRestriction = inClause + " (SELECT relation.executor.id FROM " + ExecutorGroupMembership.class.getName()
                + " as relation WHERE relation.group.id=" + group.getId() + ")";
        String[] idRestrictions = { notInRestriction, "<> " + group.getId() };
        val permissions = new RestrictionsToPermissions(user, isExclude ? Permission.UPDATE : Permission.READ, ALL_EXECUTORS_CLASSES);
        val parameters = CompilerParameters.createPaged().addPermissions(permissions).addIdRestrictions(idRestrictions);
        return new PresentationConfiguredCompiler<>(batchPresentation, parameters);
    }

    /**
     * Create {@linkplain PresentationConfiguredCompiler} for loading executor groups. Loaded first level groups, not recursive. <b>Paging is enabled
     * on executors loading.</b>
     * 
     * @param user
     *            Current actor.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executor groups.
     * @param hasGroup
     *            Flag equals true, if loading groups, which already contains executor; false to load groups, which doesn't contains executor.
     * @return {@linkplain PresentationConfiguredCompiler} for loading executor groups.
     */
    public static PresentationConfiguredCompiler<Group> createExecutorGroupsCompiler(User user, Executor executor,
            BatchPresentation batchPresentation, boolean hasGroup) {
        String inClause = hasGroup ? "IN" : "NOT IN";
        String inRestriction = inClause + " (SELECT relation.group.id FROM " + ExecutorGroupMembership.class.getName()
                + " as relation WHERE relation.executor.id=" + executor.getId() + ")";
        String[] idRestrictions = { inRestriction, "<> " + executor.getId() };
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, Permission.READ,
                new SecuredObjectType[] { SecuredObjectType.EXECUTOR });
        CompilerParameters parameters = CompilerParameters.createPaged().addPermissions(permissions).addRequestedClass(Group.class)
                .addIdRestrictions(idRestrictions);
        return new PresentationConfiguredCompiler<>(batchPresentation, parameters);
    }

    /**
     * Create {@linkplain PresentationConfiguredCompiler} for loading executor's which already has (or not has) some permission on specified
     * securedObject.
     * 
     * @param user
     *            Current actor.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain SecuredObject}; false to load executors without permissions.
     * @return {@linkplain PresentationConfiguredCompiler} for loading executors.
     */
    public static PresentationConfiguredCompiler<Executor> createExecutorWithPermissionCompiler(User user, SecuredObject securedObject,
            BatchPresentation batchPresentation, boolean hasPermission) {
        String inClause = hasPermission ? "IN" : "NOT IN";
        String idRestriction = inClause + " (SELECT pm.executor.id from " + PermissionMapping.class.getName() + " as pm where pm.objectId="
                + securedObject.getSecuredObjectId() + " and pm.objectType='" + securedObject.getSecuredObjectType() + "')";
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, Permission.READ, ALL_EXECUTORS_CLASSES);
        CompilerParameters parameters = CompilerParameters.createPaged().addPermissions(permissions).addIdRestrictions(idRestriction);
        return new PresentationConfiguredCompiler<>(batchPresentation, parameters);
    }
}
