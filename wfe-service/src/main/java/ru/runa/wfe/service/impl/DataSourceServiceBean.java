package ru.runa.wfe.service.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.service.decl.DataSourceServiceLocal;
import ru.runa.wfe.service.decl.DataSourceServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DataSourceAPI", serviceName = "DataSourceWebService")
@SOAPBinding
public class DataSourceServiceBean implements DataSourceServiceLocal, DataSourceServiceRemote {

    @Autowired
    protected ExecutorDao executorDAO;

    @Override
    @WebResult(name = "result")
    public List<String> getNames() {
        return DataSourceStorage.getNames();
    }

    @Override
    @WebResult(name = "result")
    public void importDataSource(@WebParam(name = "user") User user, @WebParam(name = "archive") byte[] archive) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorDAO.isAdministrator(user.getActor()), "not administrator");
        Preconditions.checkArgument(archive != null, "archive");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archive), Charsets.UTF_8)) {
            zis.getNextEntry();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, 1024)) > -1) {
                baos.write(buf, 0, n);
            }
            zis.closeEntry();
            DataSourceStorage.save(baos.toByteArray());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    @WebResult(name = "result")
    public byte[] exportDataSource(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorDAO.isAdministrator(user.getActor()), "not administrator");
        Preconditions.checkArgument(name != null, "name");
        byte[] content = DataSourceStorage.restoreWithoutPassword(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, Charsets.UTF_8)) {
            ZipEntry entry = new ZipEntry(name + ".xml");
            entry.setSize(content.length);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return baos.toByteArray();
    }

    @Override
    @WebMethod(exclude = true)
    public void removeDataSource(@WebParam(name = "user") User user, @WebParam(name = "id") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorDAO.isAdministrator(user.getActor()), "not administrator");
        Preconditions.checkArgument(name != null, "name");
        DataSourceStorage.moveToHistory(name);
    }
}
