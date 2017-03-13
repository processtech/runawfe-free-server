/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 19.04.2006
 * 
 */
public class LDAPImporterClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: LDAPSynchronizerClient <username> <password>");
            System.out.println("Example: LDAPSynchronizerClient foo secretword");
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
        Delegates.getSynchronizationService().synchronizeExecutorsWithLDAP(user, true, true, true);
    }
}
