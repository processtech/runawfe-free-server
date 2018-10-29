package ru.runa.af;

import java.util.List;

import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.PermissionNotFoundException;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.GroupPermission;

/**
 * Created on 14.07.2004
 * 
 */
public class PermissionTest extends ServletTestCase {

    public static TestSuite suite() {
        return new TestSuite(PermissionTest.class);
    }

    final public void testAllPermissionsHaveDifferentMasks() {
        List<Permission> pa;
        pa = new SystemPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission mask", pa.get(i).getMask() == pa.get(j).getMask());
            }
        }

        pa = new ActorPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission mask", pa.get(i).getMask() == pa.get(j).getMask());
            }
        }

        pa = new GroupPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission mask", pa.get(i).getMask() == pa.get(j).getMask());
            }
        }
    }

    final public void testAllPermissionsHaveDifferentNames() {
        List<Permission> pa;

        pa = new SystemPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission name", pa.get(i).getName().equals(pa.get(j).getName()));
            }
        }

        pa = new ActorPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission name", pa.get(i).getName().equals(pa.get(j).getName()));
            }
        }

        pa = new GroupPermission().getAllPermissions();
        for (int i = 0; i < pa.size(); i++) {
            for (int j = i + 1; j < pa.size(); j++) {
                assertFalse("Equal Permission name", pa.get(i).getName().equals(pa.get(j).getName()));
            }
        }
    }

    final public void testGetPermissionByMask() throws Exception {
        long mask;
        mask = Permission.READ.getMask();
        assertEquals(Permission.READ, new Permission().getPermission(mask));

        mask = ActorPermission.UPDATE.getMask();
        assertEquals(ActorPermission.UPDATE, new ActorPermission().getPermission(mask));

        mask = GroupPermission.ADD_TO_GROUP.getMask();
        assertEquals(GroupPermission.ADD_TO_GROUP, new GroupPermission().getPermission(mask));

        mask = SystemPermission.CREATE_EXECUTOR.getMask();
        assertEquals(SystemPermission.CREATE_EXECUTOR, new SystemPermission().getPermission(mask));

        try {
            new Permission().getPermission(-1);
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new ActorPermission().getPermission(-1);
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new GroupPermission().getPermission(-1);
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new SystemPermission().getPermission(-1);
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
    }

    final public void testGetPermissionByName() throws Exception {
        String name;
        name = Permission.READ.getName();
        assertEquals(Permission.READ, new Permission().getPermission(name));

        name = ActorPermission.UPDATE.getName();
        assertEquals(ActorPermission.UPDATE, new ActorPermission().getPermission(name));

        name = GroupPermission.ADD_TO_GROUP.getName();
        assertEquals(GroupPermission.ADD_TO_GROUP, new GroupPermission().getPermission(name));

        name = SystemPermission.CREATE_EXECUTOR.getName();
        assertEquals(SystemPermission.CREATE_EXECUTOR, new SystemPermission().getPermission(name));

        try {
            new Permission().getPermission("unexisting");
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new ActorPermission().getPermission("unexisting");
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new GroupPermission().getPermission("unexisting");
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
        try {
            new SystemPermission().getPermission("unexisting");
            fail("get unexisting permission");
        } catch (PermissionNotFoundException e) {
        }
    }

    final public void testEquals() {
        assertTrue("Wrong equals()", ActorPermission.READ.equals(ActorPermission.READ));
        assertFalse("Wrong equals()", ActorPermission.READ.equals(null));
        assertFalse("Wrong equals()", ActorPermission.READ.equals(ActorPermission.UPDATE));
        assertTrue("Wrong equals()", ActorPermission.READ.equals(Permission.READ));

        assertTrue("Wrong equals()", GroupPermission.READ.equals(GroupPermission.READ));
        assertFalse("Wrong equals()", GroupPermission.READ.equals(null));
        assertFalse("Wrong equals()", GroupPermission.READ.equals(GroupPermission.UPDATE));
        assertTrue("Wrong equals()", GroupPermission.READ.equals(Permission.READ));

        assertTrue("Wrong equals()", SystemPermission.READ.equals(SystemPermission.READ));
        assertFalse("Wrong equals()", SystemPermission.READ.equals(null));
        assertFalse("Wrong equals()", SystemPermission.READ.equals(SystemPermission.CREATE_EXECUTOR));
        assertTrue("Wrong equals()", SystemPermission.READ.equals(Permission.READ));
    }

}
