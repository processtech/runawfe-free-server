package ru.runa.wfe.service.impl;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.service.decl.DigitalSignatureServiceLocal;
import ru.runa.wfe.service.decl.DigitalSignatureServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.*;
import ru.runa.wfe.digitalsignature.logic.DigitalSignatureLogic;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Implements DigitalSignatureService as a bean.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DigitalSignatureAPI", serviceName = "DigitalSignatureWebService")
@SOAPBinding
@SuppressWarnings("unchecked")
public class DigitalSignatureServiceBean implements DigitalSignatureServiceLocal, DigitalSignatureServiceRemote {
    @Autowired
    private DigitalSignatureLogic digitalSignatureLogic;

    @Override
    @WebResult(name = "result")
    public DigitalSignature create(@WebParam(name = "user") @NonNull User user, @WebParam(name = "digitalSignature") @NonNull DigitalSignature digitalSignature) {
        return digitalSignatureLogic.create(user, digitalSignature);
    }

    @Override
    @WebResult(name = "result")
    public DigitalSignature getDigitalSignature(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        return digitalSignatureLogic.getDigitalSignature(user, id);
    }

    @Override
    @WebResult(name = "result")
    public void update(@WebParam(name = "user") @NonNull User user, @WebParam(name = "digitalSignature") @NonNull DigitalSignature digitalSignature) {
        digitalSignatureLogic.update(user, digitalSignature);
    }
    @Override
    @WebResult(name = "result")
    public void updateRoot(@WebParam(name = "user") @NonNull User user, @WebParam(name = "digitalSignature") @NonNull DigitalSignature digitalSignature) {
        digitalSignatureLogic.updateRoot(user, digitalSignature);
    }

    @Override
    @WebResult(name = "result")
    public void remove(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        digitalSignatureLogic.remove(user, digitalSignatureLogic.getDigitalSignature(user, id));
    }

    @Override
    @WebResult(name = "result")
    public boolean doesDigitalSignatureExist(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        return digitalSignatureLogic.doesDigitalSignatureExist(user, id);
    }

    @Override
    public boolean doesRootDigitalSignatureExist(User user) {
       return digitalSignatureLogic.doesRootDigitalSignatureExist(user);
    }

    @Override
    public byte[] getRootCertificate(User user) {
        return digitalSignatureLogic.getRootCertificate(user);
    }

    @Override
    public DigitalSignature createRoot(User loggedUser, DigitalSignature digitalSignature) {
        return digitalSignatureLogic.createRoot(digitalSignature);
    }

    public DigitalSignature getRootDigitalSignature(User user) {
        return digitalSignatureLogic.getRootDigitalSignature(user);
    }

    @Override
    public void removeRootDigitalSignature(User user) {
        digitalSignatureLogic.remove(user, digitalSignatureLogic.getRootDigitalSignature(user));
    }

}
