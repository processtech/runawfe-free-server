package ru.runa.wfe.security;

import java.util.List;

import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.report.ReportPermission;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.GroupPermission;

public enum SecuredObjectType {
    NONE(Permission.class), SYSTEM(WorkflowSystemPermission.class), BOTSTATION(BotStationPermission.class), ACTOR(ActorPermission.class), GROUP(
            GroupPermission.class), RELATION(RelationPermission.class), RELATIONGROUP(RelationPermission.class), RELATIONPAIR(
                    RelationPermission.class), DEFINITION(DefinitionPermission.class), PROCESS(
                            ProcessPermission.class), REPORT(ReportPermission.class);

    private Class<? extends Permission> permissionClass;

    private SecuredObjectType(Class<? extends Permission> permissionClass) {
        this.permissionClass = permissionClass;
    }

    // TODO find another way
    public Permission getNoPermission() {
        return ClassLoaderUtil.instantiate(permissionClass);
    }

    public List<Permission> getAllPermissions() {
        return getNoPermission().getAllPermissions();
    }
}
