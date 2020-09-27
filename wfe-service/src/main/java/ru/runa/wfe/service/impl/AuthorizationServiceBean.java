package ru.runa.wfe.service.impl;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.service.decl.AuthorizationServiceLocal;
import ru.runa.wfe.service.decl.AuthorizationServiceRemote;
import ru.runa.wfe.service.decl.AuthorizationWebServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Implements AuthorizationService as bean. Created on 20.07.2004
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "AuthorizationAPI", serviceName = "AuthorizationWebService")
@SOAPBinding
public class AuthorizationServiceBean implements AuthorizationServiceLocal, AuthorizationServiceRemote, AuthorizationWebServiceRemote {
    @Autowired
    private AuthorizationLogic authorizationLogic;
    @Autowired
    private PermissionDao permissionDao;

    @Override
    @WebMethod(exclude = true)
    public void checkAllowed(@NonNull User user, @NonNull Permission permission, @NonNull SecuredObject securedObject) {
        permissionDao.checkAllowed(user, permission, securedObject);
    }

    @Override
    @WebMethod(exclude = true)
    public void checkAllowed(@NonNull User user, @NonNull Permission permission, @NonNull SecuredObjectType type, @NonNull Long id) {
        permissionDao.checkAllowed(user, permission, type, id);
    }

    @Override
    @WebResult(name = "result")
    public boolean isAllowed(@WebParam(name = "user") @NonNull User user, @WebParam(name = "permission") @NonNull Permission permission,
            @WebParam(name = "identifiable") @NonNull SecuredObject securedObject) {
        return permissionDao.isAllowed(user, permission, securedObject);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean isAllowed(@WebParam(name = "user") @NonNull User user, @WebParam(name = "permission") @NonNull Permission permission,
            @WebParam(name = "securedObjectType") @NonNull SecuredObjectType securedObjectType,
            @WebParam(name = "identifiableId") @NonNull Long identifiableId) {
        return authorizationLogic.isAllowed(user, permission, securedObjectType, identifiableId);
    }

    @WebMethod(exclude = true)
    @Override
    public <T extends SecuredObject> boolean[] isAllowed(@NonNull User user, @NonNull Permission permission, @NonNull List<T> securedObjects) {
        Preconditions.checkArgument(!securedObjects.contains(null), "securedObjects element");
        return authorizationLogic.isAllowed(user, permission, securedObjects);
    }
    
    @WebMethod(exclude = true)
    @Override
    public Set<Long> filterAllowedIds(Executor executor, Permission permission, SecuredObjectType securedObjectType, List<Long> idsOrNull) {
        return authorizationLogic.selectAllowedIds(executor, permission, securedObjectType, idsOrNull);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean[] isAllowed(
            @NonNull User user,
            @NonNull Permission permission,
            @NonNull SecuredObjectType type,
            @NonNull List<Long> ids
    ) {
        Preconditions.checkArgument(!ids.contains(null), "ids element");
        return authorizationLogic.isAllowed(user, permission, type, ids);
    }

    @WebMethod(exclude = true)
    @Override
    public boolean isAllowedForAny(@WebParam(name = "user") @NonNull User user, @WebParam(name = "permission") @NonNull Permission permission,
            @WebParam(name = "securedObjectType") @NonNull SecuredObjectType securedObjectType) {
        return authorizationLogic.isAllowedForAny(user, permission, securedObjectType);
    }

    @Override
    @WebResult(name = "result")
    public List<Permission> getIssuedPermissions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "performer") @NonNull Executor performer, @WebParam(name = "identifiable") @NonNull SecuredObject securedObject) {
        return authorizationLogic.getIssuedPermissions(user, performer, securedObject);
    }

    @WebMethod(exclude = true)
    @Override
    public void exportDataFile(User user, Document script) {
        authorizationLogic.exportDataFile(user, script);
    }

    @WebMethod(exclude = true)
    @Override
    public void addPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        authorizationLogic.addPermissions(user, executorName, objectNames, permissions);
    }

    @WebMethod(exclude = true)
    @Override
    public void removePermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        authorizationLogic.removePermissions(user, executorName, objectNames, permissions);
    }

    @WebMethod(exclude = true)
    @Override
    public void removeAllPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames) {
        authorizationLogic.removeAllPermissions(user, executorName, objectNames);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        authorizationLogic.setPermissions(user, executorName, objectNames, permissions);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(@NonNull User user, @NonNull List<Long> executorIds, @NonNull List<Collection<Permission>> permissions,
            @NonNull SecuredObject securedObject) {
        authorizationLogic.setPermissions(user, executorIds, permissions, securedObject);
    }

    @Override
    @WebResult(name = "result")
    public void setPermissions(@NonNull @WebParam(name = "user") User user, @NonNull @WebParam(name = "executorId") Long executorId,
            @WebParam(name = "permissions") @NonNull Collection<Permission> permissions,
            @WebParam(name = "identifiable") @NonNull SecuredObject securedObject) {
        authorizationLogic.setPermissions(user, executorId, permissions, securedObject);
    }

    @WebMethod(exclude = true)
    @Override
    public void setPermissions(@NonNull User user, @NonNull List<Long> executorsId, @NonNull Collection<Permission> permissions,
            @NonNull SecuredObject securedObject) {
        authorizationLogic.setPermissions(user, executorsId, permissions, securedObject);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getExecutorsWithPermission(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "identifiable") @NonNull SecuredObject securedObject,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "withPermission") boolean withPermission) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return (List<Executor>) authorizationLogic.getExecutorsWithPermission(user, securedObject, batchPresentation, withPermission);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorsWithPermissionCount(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "identifiable") @NonNull SecuredObject securedObject,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "withPermission") boolean withPermission) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return authorizationLogic.getExecutorsWithPermissionCount(user, securedObject, batchPresentation, withPermission);
    }

    @Override
    @SuppressWarnings("unchecked")
    @WebResult(name = "result")
    public <T extends Object> List<T> getPersistentObjects(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") @NonNull BatchPresentation batchPresentation,
            @WebParam(name = "persistentClass") @NonNull Class<T> persistentClass,
            @WebParam(name = "permission") @NonNull Permission permission,
            @WebParam(name = "securedObjectTypes") @NonNull SecuredObjectType[] securedObjectTypes,
            @WebParam(name = "enablePaging") boolean enablePaging) {
        return (List<T>) authorizationLogic.getPersistentObjects(user, batchPresentation, permission, securedObjectTypes, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public SecuredObject findSecuredObject(@WebParam(name = "type") @NonNull SecuredObjectType type, @WebParam(name = "id") Long id) {
        return authorizationLogic.findSecuredObject(type, id);
    }

    @Override
    @WebResult(name = "result")
    public boolean isAllowedWS(@WebParam(name = "user") @NonNull User user, @WebParam(name = "permission") @NonNull Permission permission,
            @WebParam(name = "securedObjectType") @NonNull SecuredObjectType securedObjectType,
            @WebParam(name = "identifiableId") @NonNull Long identifiableId) {
        return authorizationLogic.isAllowed(user, permission, securedObjectType, identifiableId);
    }
}
