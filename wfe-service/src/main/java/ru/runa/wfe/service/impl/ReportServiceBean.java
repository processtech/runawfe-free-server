package ru.runa.wfe.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.logic.ReportLogic;
import ru.runa.wfe.service.decl.ReportServiceLocal;
import ru.runa.wfe.service.decl.ReportServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

@Stateless(name = "ReportServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ReportAPI", serviceName = "ReportWebService")
@SOAPBinding
public class ReportServiceBean implements ReportServiceLocal, ReportServiceRemote {
    @Autowired
    private ReportLogic reportLogic;

    @Override
    @WebResult(name = "result")
    public List<WfReport> getReportDefinitions(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.REPORTS.createNonPaged();
        }
        return reportLogic.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public WfReport getReportDefinition(@WebParam(name = "user") User user, @WebParam(name = "id") Long id) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(id != null, "id");
        return reportLogic.getReportDefinition(user, id);
    }

    @Override
    @WebResult(name = "result")
    public List<WfReportParameter> analyzeReportFile(@WebParam(name = "report") WfReport report,
            @WebParam(name = "reportFileContent") byte[] reportFileContent) {
        Preconditions.checkArgument(report != null, "report");
        Preconditions.checkArgument(reportFileContent != null, "reportFileContent");
        return reportLogic.analyzeReportFile(report, reportFileContent);
    }

    @Override
    @WebResult(name = "result")
    public void deployReport(@WebParam(name = "user") User user, @WebParam(name = "report") WfReport report, @WebParam(name = "file") byte[] file) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(report != null, "report");
        Preconditions.checkArgument(file != null, "file");
        reportLogic.deployReport(user, report, file);
    }

    @Override
    @WebResult(name = "result")
    public void redeployReport(@WebParam(name = "user") User user, @WebParam(name = "report") WfReport report, @WebParam(name = "file") byte[] file) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(report != null, "report");
        reportLogic.redeployReport(user, report, file);
    }

    @Override
    @WebResult(name = "result")
    public void undeployReport(@WebParam(name = "user") User user, @WebParam(name = "reportId") Long reportId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(reportId != null, "reportId");
        reportLogic.undeployReport(user, reportId);
    }
}
