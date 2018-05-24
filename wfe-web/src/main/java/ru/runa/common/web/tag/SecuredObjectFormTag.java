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
package ru.runa.common.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Has "identifiableId" attribute; SecuredObjectType is obtained via abstract method.
 *
 * @see SecuredObjectFormTag2
 */
public abstract class SecuredObjectFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;
    private Long identifiableId;

    @Attribute
    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    public Long getIdentifiableId() {
        return identifiableId;
    }

    protected abstract void fillFormData(final TD tdFormElement);

    /**
     * @return {@link Permission} that executor must have to update.
     */
    protected abstract Permission getSubmitPermission();

    protected abstract SecuredObject getSecuredObject();

    @Override
    protected boolean isSubmitButtonEnabled() {
        Permission permission = getSubmitPermission();
        if (permission == null) {
            return true;
        }
        return isSubmitButtonEnabled(getSecuredObject(), permission);
    }

    protected boolean isSubmitButtonEnabled(SecuredObject securedObject, Permission permission) {
        return Delegates.getAuthorizationService().isAllowed(getUser(), permission, securedObject);
    }

    @Override
    public final void fillFormElement(TD tdFormElement) {
        fillFormData(tdFormElement);
        Input hiddenName = new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(getIdentifiableId()));
        tdFormElement.addElement(hiddenName);
    }
}
