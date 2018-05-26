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
package ru.runa.common.web.html;

import java.io.Serializable;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.User;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public interface TDBuilder {

    interface Env {
        interface SecuredObjectExtractor extends Serializable {
            SecuredObject getSecuredObject(Object o, Env env);
        }

        final class IdentitySecuredObjectExtractor<T extends SecuredObject> implements SecuredObjectExtractor {
            @Override
            public SecuredObject getSecuredObject(Object o, Env env) {
                return (T)o;
            }
        }

        User getUser();

        PageContext getPageContext();

        BatchPresentation getBatchPresentation();

        String getURL(Object object);

        String getConfirmationMessage(Long pid);

        boolean isAllowed(Permission permission, ru.runa.common.web.html.TDBuilder.Env.SecuredObjectExtractor extractor);

        boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId);

    }

    TD build(Object object, Env env);

    String getValue(Object object, Env env);

    String[] getSeparatedValues(Object object, Env env);

    int getSeparatedValuesCount(Object object, Env env);
}
