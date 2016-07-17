package ru.runa.wfe.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportFileMissingException;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.report.dto.ReportParameterDto;
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
    public List<ReportDto> getReportDefinitions(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(batchPresentation != null);
        return reportLogic.getReportDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    public ReportDto getReportDefinition(@WebParam(name = "user") User user, @WebParam(name = "id") Long id) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(id != null);
        return reportLogic.getReportDefinition(user, id);
    }

    @Override
    public List<ReportParameterDto> analyzeReportFile(@WebParam(name = "report") ReportDto report,
            @WebParam(name = "reportFileContent") byte[] reportFileContent) {
        Preconditions.checkArgument(report != null);
        Preconditions.checkArgument(reportFileContent != null);
        return reportLogic.analyzeReportFile(report, reportFileContent);
    }

    @Override
    public void deployReport(@WebParam(name = "user") User user, @WebParam(name = "report") ReportDto report, @WebParam(name = "file") byte[] file) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(report != null);
        Preconditions.checkArgument(file != null);
        reportLogic.deployReport(user, report, file);
    }

    @Override
    public void redeployReport(User user, ReportDto report, byte[] file) throws ReportFileMissingException {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(report != null);
        reportLogic.redeployReport(user, report, file);
    }

    @Override
    public void undeployReport(User user, Long reportId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(reportId != null);
        reportLogic.undeployReport(user, reportId);
    }
}
