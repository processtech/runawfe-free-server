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
package ru.runa.wfe.security.auth;

import com.google.common.base.Preconditions;
import java.security.Key;
import java.util.Arrays;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.security.auth.Subject;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * Helps to extract {@link Actor} from Subject principals at logic layer.
 */
@CommonsLog
public class SubjectPrincipalsHelper {

    private static Key securedKey = null;
    private static String encryptionType = "DES";

    static {
        try {
            securedKey = KeyGenerator.getInstance(encryptionType).generateKey();
        } catch (Exception e) {
            log.error("Unable to get instance of KeyGenerator", e);
        }
    }

    private SubjectPrincipalsHelper() {
    }

    private static byte[] getActorKey(Actor actor) {
        return actor.getName().getBytes();
    }

    public static User createUser(Actor actor) {
        try {
            Cipher cipher = Cipher.getInstance(encryptionType);
            cipher.init(Cipher.ENCRYPT_MODE, securedKey);
            byte[] securedKey = cipher.doFinal(getActorKey(actor));
            return new User(actor, securedKey);
        } catch (Exception e) {
            log.warn("Can't create subject cipher");
            return null;
        }
    }

    public static void validateUser(User user) throws AuthenticationExpiredException {
        try {
            Cipher cipher = Cipher.getInstance(encryptionType);
            cipher.init(Cipher.DECRYPT_MODE, securedKey);
            if (!Arrays.equals(getActorKey(user.getActor()), cipher.doFinal(user.getSecuredKey()))) {
                throw new AuthenticationExpiredException("Incorrect user principal: secured key validation has been failed");
            }
        } catch (Exception e) {
            log.warn("Error in subject decryption: " + e);
            throw new AuthenticationExpiredException("Error in subject decryption");
        }
    }

    public static User getUser(Subject subject) throws AuthenticationException {
        Preconditions.checkNotNull(subject);
        Set<User> principals = subject.getPrincipals(User.class);
        for (User user : principals) {
            if (user != null) {
                return user;
            }
        }
        throw new AuthenticationException("Subject does not contain user principal");
    }

}
