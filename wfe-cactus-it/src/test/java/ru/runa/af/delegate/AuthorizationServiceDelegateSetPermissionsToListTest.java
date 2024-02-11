package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import lombok.val;
import lombok.var;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

public class AuthorizationServiceDelegateSetPermissionsToListTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;
    private ExecutorService executorService;

    private List<Collection<Permission>> legalPermissions = null;
    private List<Long> legalActorIds = null;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        h.createDefaultExecutorsMap();
        authorizationService = Delegates.getAuthorizationService();
        executorService = Delegates.getExecutorService();

        // setPermissions() requires READ for subject and UPDATE_PERMISSIONS for object:
        val ppR = Lists.newArrayList(Permission.READ);
        var ppU = Lists.newArrayList(Permission.UPDATE_PERMISSIONS);
        // For executors who act as both subject and object:
        var ppRU = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);

        h.setPermissionsToAuthorizedActor(ppU, h.getBaseGroup());
        h.setPermissionsToAuthorizedActor(ppRU, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(ppR, h.getSubGroup());
        h.setPermissionsToAuthorizedActor(ppR, h.getSubGroupActor());
        h.setPermissionsToAuthorizedActor(ppU, SecuredSingleton.SYSTEM);

        legalActorIds = Lists.newArrayList(h.getBaseGroupActor().getId(), h.getSubGroupActor().getId());
        legalPermissions = Lists.newArrayList(ppR, ppR);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
        executorService = null;
    }

    public void testSetPermissionsFakeUser() {
        try {
            authorizationService.setPermissions(h.getFakeUser(), legalActorIds, legalPermissions, SecuredSingleton.SYSTEM);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testSetPermissions() {
        authorizationService.setPermissions(h.getAuthorizedUser(), legalActorIds, legalPermissions, SecuredSingleton.SYSTEM);
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(h.getAdminUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(h.getAdminUser(), executor, SecuredSingleton.SYSTEM);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on SYSTEM",
                    legalPermissions.get(i), actual);
        }

        authorizationService.setPermissions(h.getAuthorizedUser(), legalActorIds, legalPermissions, h.getBaseGroup());
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(h.getAdminUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(h.getAdminUser(), executor, h.getBaseGroup());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on base group actor",
                    legalPermissions.get(i), actual);
        }

        legalPermissions = Lists.newArrayList(
                Lists.newArrayList(Permission.READ, Permission.UPDATE_ACTOR_STATUS),
                Lists.newArrayList(Permission.READ, Permission.UPDATE_ACTOR_STATUS)
        );
        authorizationService.setPermissions(h.getAuthorizedUser(), legalActorIds, legalPermissions, h.getBaseGroupActor());
        for (int i = 0; i < legalActorIds.size(); i++) {
            Executor executor = executorService.getExecutor(h.getAdminUser(), legalActorIds.get(i));
            Collection<Permission> actual = authorizationService.getIssuedPermissions(h.getAdminUser(), executor, h.getBaseGroupActor());
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions on base group actor",
                    legalPermissions.get(i), actual);
        }
    }

    public void testSetPermissionsUnauthorized() {
        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), legalActorIds, legalPermissions, SecuredSingleton.SYSTEM);
            fail("AuthorizationDelegate.setPermission allows unauthorized subject");
        } catch (AuthorizationException e) {
            // Expected.
        }

        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), legalActorIds, legalPermissions, h.getBaseGroup());
            fail("AuthorizationDelegate.setPermission allows unauthorized subject");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
