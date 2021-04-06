package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/**
 * Created on 16.02.2005.
 */
public class AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;

    private Group additionalGroup;
    private Actor additionalActor;
    private List<Executor> additionalActorGroupsMixed;
    private List<Long> executorIDs;

    private Collection<Permission> expected = Lists.newArrayList(Permission.READ, Permission.UPDATE);

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();

        // authorizationService.setPermissions() requires READ on subject (mixed executors) and UPDATE_PERMISSIONS on object (additional executors).
        val ppSubject = Lists.newArrayList(Permission.READ);
        val ppObject = Lists.newArrayList(Permission.UPDATE_PERMISSIONS);

        additionalActor = h.createActorIfNotExist("additionalA", "Additional Actor");
        additionalGroup = h.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActorGroupsMixed = h.createMixedActorsGroupsArray("mixed", "Additional Mixed");
        executorIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorIDs.add(executor.getId());
            h.setPermissionsToAuthorizedActor(ppSubject, executor);
        }

        h.setPermissionsToAuthorizedActor(ppObject, additionalActor);
        h.setPermissionsToAuthorizedActor(ppObject, additionalGroup);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testSetPermissions() {
        authorizationService.setPermissions(h.getAuthorizedUser(), executorIDs, expected, additionalActor);
        for (int i = 0; i < executorIDs.size(); i++) {
            val actual = authorizationService.getIssuedPermissions(h.getAdminUser(), additionalActorGroupsMixed.get(i), additionalActor);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);
        }

        authorizationService.setPermissions(h.getAuthorizedUser(), executorIDs, expected, additionalGroup);
        for (int i = 0; i < executorIDs.size(); i++) {
            val actual = authorizationService.getIssuedPermissions(h.getAdminUser(), additionalActorGroupsMixed.get(i), additionalGroup);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", expected, actual);
        }
    }

    public void testSetPermissionsFakeUser() {
        try {
            authorizationService.setPermissions(h.getFakeUser(), executorIDs, expected, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testSetPermissionsUnauthorizedUser() {
        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), executorIDs, expected, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }

        try {
            authorizationService.setPermissions(h.getUnauthorizedUser(), executorIDs, expected, additionalGroup);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
