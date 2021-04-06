package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 19.04.2006
 *
 */
public class LdapImporterClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: LdapImporterClient <username> <password>");
            System.out.println("Example: LdapImporterClient foo secretword");
            System.exit(-1);
        }
        try {
            importExecutors(args[0], args[1]);
        } catch (Exception e) {
            // this execption handling is nessesery here.
            System.out.println(e.getMessage());
            // e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void importExecutors(String username, String password) {
        User user = Delegates.getAuthenticationService().authenticateByLoginPassword(username, password);
        Delegates.getSynchronizationService().synchronizeExecutorsWithLdap(user);
    }
}
