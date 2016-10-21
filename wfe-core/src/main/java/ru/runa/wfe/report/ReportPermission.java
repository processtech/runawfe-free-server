package ru.runa.wfe.report;

import java.util.List;

import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

/**
 * Permissions for report subsystem.
 */
public class ReportPermission extends Permission {
    private static final long serialVersionUID = 1L;

    /**
     * Deploy/undeploy report permission.
     */
    public static final Permission DEPLOY = new ReportPermission(2, "permission.report.deploy");

    /**
     * All permissions, declared for reports.
     */
    private static final List<Permission> REPORT_PERMISSIONS = fillPermissions();

    public ReportPermission() {
        super();
    }

    public ReportPermission(int maskPower, String name) {
        super(maskPower, name);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(REPORT_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(DEPLOY);
        return result;
    }
}
