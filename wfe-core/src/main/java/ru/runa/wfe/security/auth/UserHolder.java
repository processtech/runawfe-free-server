package ru.runa.wfe.security.auth;

import ru.runa.wfe.user.User;

/**
 * This class holds user from EJB call and should be used only in action
 * handlers in extravagant cases.
 * 
 * @author dofs
 * @since 4.0
 */
public class UserHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public static void set(User user) {
        users.set(user);
    }

    public static User get() {
        return users.get();
    }

    public static void reset() {
        users.remove();
    }
}
