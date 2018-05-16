package ru.runa.wfe.report.dao;

import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToPermissions;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportWithNameExistsException;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.User;

public class ReportDAO extends GenericDAO<ReportDefinition> {

    private static final SecuredObjectType[] SECURED_OBJECTS = new SecuredObjectType[] { SecuredObjectType.REPORT };

    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, Permission.READ, SECURED_OBJECTS);
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addPermissions(permissions);
        List<ReportDefinition> deployments = new PresentationCompiler<ReportDefinition>(batchPresentation).getBatch(parameters);
        List<WfReport> definitions = Lists.newArrayList();
        for (ReportDefinition deployment : deployments) {
            definitions.add(new WfReport(deployment));
        }
        return definitions;
    }

    public WfReport getReportDefinition(Long id) {
        return new WfReport(this.get(id));
    }

    public ReportDefinition getReportDefinition(String reportName) {
        return findFirstOrNull("from " + ReportDefinition.class.getName() + " where name=?", reportName);
    }

    public void deployReport(ReportDefinition report) {
        if (getReportDefinition(report.getName()) != null) {
            throw new ReportWithNameExistsException(report.getName());
        }
        this.create(report);
    }

    public void redeployReport(ReportDefinition reportDefinition) {
        ReportDefinition def = get(reportDefinition.getId());
        getHibernateTemplate().deleteAll(def.getParameters());
        def.updateFrom(reportDefinition);
        this.update(def);
    }

    public void undeploy(Long reportId) {
        this.delete(reportId);
    }
}
