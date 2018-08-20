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
package ru.runa.af.web.action;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jcifs.UniAddress;
import jcifs.http.NtlmSsp;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * This class provides NTLM SSO for Firefox and probably for IE with fixed bag
 * http ://support.microsoft.com/default.aspx?scid=kb;en-us;902409&sd=rss&spid=
 * 2073. In order to enable SSO support in IE enable
 * options/advances/security/Enable Integrated Windows Authentication Created on
 * 10.11.2005
 * 
 * @struts:action path="/ntlmlogin"
 * @struts.action-forward name="success" path="/manage_tasks.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/start.do" redirect = "true"
 */
public class NtlmLoginAction extends ActionBase {

    /* this must be changed if "success" forward changed! */
    private final static String DEFAULT_TAB_FORWARD_NAME = "manage_tasks";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!WebResources.isNTLMSupported()) {
                throw new AuthenticationException("NTLM support disabled");
            }
            NtlmPasswordAuthentication ntlmPasswordAuthentication = getNTLMPasswordAuthentication(request, response);
            if (ntlmPasswordAuthentication == null) {
                return null;
            }
            UniAddress dc = UniAddress.getByName(WebResources.getDomainName(), true);
            SmbSession.logon(dc, ntlmPasswordAuthentication);
            String actorName = ntlmPasswordAuthentication.getUsername();
            Actor actor = Delegates.getExecutorService().getActorCaseInsensitive(actorName);
            User user = SubjectPrincipalsHelper.createUser(actor);
            Delegates.getSystemService().login(user);
            Commons.setUser(user, request.getSession());
            ProfileHttpSessionHelper.reloadProfile(request.getSession());
            TabHttpSessionHelper.setTabForwardName(DEFAULT_TAB_FORWARD_NAME, request.getSession());
            saveToken(request);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    public static final NtlmPasswordAuthentication getNTLMPasswordAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws SmbException, UnknownHostException, IOException, ServletException {
        byte[] challenge = SmbSession.getChallenge(UniAddress.getByName(WebResources.getDomainName(), true));
        return NtlmSsp.authenticate(request, response, challenge);
    }

}
