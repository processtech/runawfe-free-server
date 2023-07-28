package ru.runa.wfe.report.dao;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToPermissions;
import ru.runa.wfe.report.QReportDefinition;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportWithNameExistsException;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.User;

@Component
public class ReportDefinitionDao extends GenericDao<ReportDefinition> {

    private static final SecuredObjectType[] SECURED_OBJECTS = new SecuredObjectType[] { SecuredObjectType.REPORT };

    public ReportDefinitionDao() {
        super(ReportDefinition.class);
    }

    public List<WfReport> getReportDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        RestrictionsToPermissions permissions = new RestrictionsToPermissions(user, Permission.READ, SECURED_OBJECTS);
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addPermissions(permissions);
        List<ReportDefinition> reportDefs = new PresentationCompiler<ReportDefinition>(batchPresentation).getBatch(parameters);
        List<WfReport> result = Lists.newArrayList();
        for (ReportDefinition rd : reportDefs) {
            result.add(new WfReport(rd));
        }
        return result;
    }

    public WfReport getReportDefinition(Long id) {
        return new WfReport(this.get(id));
    }

    public ReportDefinition getReportDefinition(String reportName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", reportName);
        return findFirstOrNull("from " + ReportDefinition.class.getName() + " where name=:name", parameters);
    }

    public void deployReport(ReportDefinition report) {
        if (getReportDefinition(report.getName()) != null) {
            throw new ReportWithNameExistsException(report.getName());
        }
        this.create(report);
    }

    public void redeployReport(ReportDefinition reportDefinition) {
        this.update(reportDefinition);
    }

    public void undeploy(Long reportId) {
        this.delete(reportId);
    }

    public long getAllCount() {
        QReportDefinition rd = QReportDefinition.reportDefinition;
        return queryFactory.selectFrom(rd).fetchCount();
    }
}
