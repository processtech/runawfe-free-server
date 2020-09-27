package ru.runa.wfe.service.impl;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.security.logic.AuthenticationLogic;
import ru.runa.wfe.service.decl.AuthenticationServiceLocal;
import ru.runa.wfe.service.decl.AuthenticationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceSimpleObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.User;

/**
 * Implements AuthenticationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceSimpleObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuthenticationAPI", serviceName = "AuthenticationWebService")
@SOAPBinding
@CommonsLog
public class AuthenticationServiceBean implements AuthenticationServiceLocal, AuthenticationServiceRemote {
    @Autowired
    private AuthenticationLogic authenticationLogic;
    @Resource
    private SessionContext context;

    @Override
    @WebResult(name = "result")
    public User authenticateByCallerPrincipal() {
        log.debug("Authenticating (principal)");
        User user = authenticationLogic.authenticate(context.getCallerPrincipal());
        log.debug("Authenticated (principal): " + user);
        return user;
    }

    @Override
    @WebResult(name = "result")
    public User authenticateByKerberos(@WebParam(name = "token") @NonNull byte[] token) {
        log.debug("Authenticating (kerberos)");
        User user = authenticationLogic.authenticate(token);
        log.debug("Authenticated (kerberos): " + user);
        return user;
    }

    @Override
    @WebResult(name = "result")
    public User authenticateByLoginPassword(@WebParam(name = "name") @NonNull String name, @WebParam(name = "password") @NonNull String password) {
        log.debug("Authenticating (login) " + name);
        User user = authenticationLogic.authenticate(name, password);
        log.debug("Authenticated (login): " + user);
        return user;
    }

    @Override
    @WebResult(name = "result")
    public User authenticateByTrustedPrincipal(@WebParam(name = "serviceUser") @NonNull User serviceUser,
            @WebParam(name = "login") @NonNull String login) {
        log.debug("Authenticating (trusted) " + login);
        User user = authenticationLogic.authenticate(serviceUser, login);
        log.debug("Authenticated (trusted): " + user);
        return user;
    }
}
