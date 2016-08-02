package ru.runa.wfe.script;

import java.util.List;

import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

/**
 * Permissions for report subsystem.
 */
public class AdmScriptPermission extends Permission {
    private static final long serialVersionUID = 1L;

    /**
     * Deploy/undeploy report permission.
     */
    public static final Permission DEPLOY = new AdmScriptPermission(2, "permission.admscript.deploy");

    /**
     * All permissions, declared for reports.
     */
    private static final List<Permission> ADM_SCRIPT_PERMISSIONS = fillPermissions();

    public AdmScriptPermission() {
        super();
    }

    public AdmScriptPermission(int maskPower, String name) {
        super(maskPower, name);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(ADM_SCRIPT_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(DEPLOY);
        return result;
    }
}
