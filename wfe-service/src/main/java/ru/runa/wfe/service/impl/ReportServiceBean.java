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
import lombok.NonNull;
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
    public List<WfReport> getReportDefinitions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.REPORTS.createNonPaged();
        }
        return reportLogic.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public WfReport getReportDefinition(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        return reportLogic.getReportDefinition(user, id);
    }

    @Override
    @WebResult(name = "result")
    public List<WfReportParameter> analyzeReportFile(@WebParam(name = "report") @NonNull WfReport report,
            @WebParam(name = "reportFileContent") @NonNull byte[] reportFileContent) {
        return reportLogic.analyzeReportFile(report, reportFileContent);
    }

    @Override
    @WebResult(name = "result")
    public void deployReport(@WebParam(name = "user") @NonNull User user, @WebParam(name = "report") @NonNull WfReport report,
            @WebParam(name = "file") @NonNull byte[] file) {
        reportLogic.deployReport(user, report, file);
    }

    @Override
    @WebResult(name = "result")
    public void redeployReport(@WebParam(name = "user") @NonNull User user, @WebParam(name = "report") @NonNull WfReport report,
            @WebParam(name = "file") byte[] file) {
        reportLogic.redeployReport(user, report, file);
    }

    @Override
    @WebResult(name = "result")
    public void undeployReport(@WebParam(name = "user") @NonNull User user, @WebParam(name = "reportId") @NonNull Long reportId) {
        reportLogic.undeployReport(user, reportId);
    }
}
