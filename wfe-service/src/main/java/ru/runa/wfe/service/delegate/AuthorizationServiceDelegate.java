package ru.runa.wfe.service.delegate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Document;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * 
 * Created 14.10.2005
 */
public class AuthorizationServiceDelegate extends Ejb3Delegate implements AuthorizationService {

    public AuthorizationServiceDelegate() {
        super(AuthorizationService.class);
    }

    private AuthorizationService getAuthorizationService() {
        return (AuthorizationService) getService();
    }

    @Override
    public void checkAllowed(User user, Permission permission, SecuredObject securedObject) {
        try {
            getAuthorizationService().checkAllowed(user, permission, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void checkAllowed(User user, Permission permission, SecuredObjectType type, Long id) {
        try {
            getAuthorizationService().checkAllowed(user, permission, type, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectTypes, Long identifiableId) {
        try {
            return getAuthorizationService().isAllowed(user, permission, securedObjectTypes, identifiableId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isAllowed(User user, Permission permission, SecuredObject securedObject) {
        try {
            return getAuthorizationService().isAllowed(user, permission, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects) {
        try {
            return getAuthorizationService().isAllowed(user, permission, securedObjects);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean[] isAllowed(User user, Permission permission, SecuredObjectType type, List<Long> ids) {
        try {
            return getAuthorizationService().isAllowed(user, permission, type, ids);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectTypes) {
        try {
            return getAuthorizationService().isAllowedForAny(user, permission, securedObjectTypes);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void exportDataFile(User user, Document script) {
        try {
            getAuthorizationService().exportDataFile(user, script);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void addPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        try {
            getAuthorizationService().addPermissions(user, executorName, objectNames, permissions);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removePermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        try {
            getAuthorizationService().removePermissions(user, executorName, objectNames, permissions);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeAllPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames) {
        try {
            getAuthorizationService().removeAllPermissions(user, executorName, objectNames);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setPermissions(User user, String executorName, Map<SecuredObjectType, Set<String>> objectNames, Set<Permission> permissions) {
        try {
            getAuthorizationService().setPermissions(user, executorName, objectNames, permissions);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject) {
        try {
            getAuthorizationService().setPermissions(user, executorId, permissions, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setPermissions(User user, List<Long> executorsId, List<Collection<Permission>> permissions, SecuredObject securedObject) {
        try {
            getAuthorizationService().setPermissions(user, executorsId, permissions, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, SecuredObject securedObject) {
        try {
            getAuthorizationService().setPermissions(user, executorsId, permissions, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject) {
        try {
            return getAuthorizationService().getIssuedPermissions(user, performer, securedObject);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Executor> getExecutorsWithPermission(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission) {
        try {
            return getAuthorizationService().getExecutorsWithPermission(user, securedObject, batchPresentation, hasPermission);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getExecutorsWithPermissionCount(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission) {
        try {
            return getAuthorizationService().getExecutorsWithPermissionCount(user, securedObject, batchPresentation, hasPermission);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public <T> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
                                                           Permission permission, SecuredObjectType[] securedObjectClasses, boolean enablePaging) {
        try {
            return getAuthorizationService().getPersistentObjects(user, batchPresentation, persistentClass, permission, securedObjectClasses,
                    enablePaging);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public SecuredObject findSecuredObject(SecuredObjectType type, Long id) {
        try {
            return getAuthorizationService().findSecuredObject(type, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Set<Long> filterAllowedIds(Executor executor, Permission permission, SecuredObjectType securedObjectType, List<Long> idsOrNull) {
        try{
            return getAuthorizationService().filterAllowedIds(executor, permission, securedObjectType, idsOrNull);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
