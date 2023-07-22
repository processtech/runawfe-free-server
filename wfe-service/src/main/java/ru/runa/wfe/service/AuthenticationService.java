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
package ru.runa.wfe.service;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.User;

/**
 * Service for authentication.
 * 
 * @since 2.0
 */
public interface AuthenticationService {

    /**
     * Integrated application server authentication.
     * 
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByCallerPrincipal() throws AuthenticationException;

    /**
     * Kerberos v5 authentication.
     * 
     * @param token
     *            kerberos token
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByKerberos(byte[] token) throws AuthenticationException;

    /**
     * Authentication by login and password against internal database.
     * 
     * @param login
     * @param password
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByLoginPassword(String login, String password) throws AuthenticationException;

    /**
     * Trusted authentication using service account with administrator
     * privileges. Requires setting
     * system.properties#trusted.authentication.enabled to true
     * 
     * @return authenticated user
     * @throws AuthenticationException
     * @since 4.2.0
     */
    public User authenticateByTrustedPrincipal(User serviceUser, String login) throws AuthenticationException;
    
    /**
     * Trusted authentication for bots using service account with administrator
     * privileges.
     * 
     * @return authenticated user
     * @throws AuthenticationException
     * @since 4.4.2
     */
    public User authenticateBot(User botStationUser, Bot bot) throws AuthenticationException;
}
