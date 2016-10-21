package ru.runa.wfe.service.decl;

import javax.ejb.Remote;

import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.user.User;

@Remote
public interface AuthorizationServiceRemoteWS extends AuthorizationService {

    boolean isAllowedWS(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId);

}
