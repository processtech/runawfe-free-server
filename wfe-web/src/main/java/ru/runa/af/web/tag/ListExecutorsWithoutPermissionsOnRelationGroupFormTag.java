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
package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.GrantPermissionsOnRelationGroupAction;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.security.Identifiable;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listExecutorsWithoutPermissionsOnRelationGroupForm")
public class ListExecutorsWithoutPermissionsOnRelationGroupFormTag extends ListExecutorsWithoutPermissionsBase {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return GrantPermissionsOnRelationGroupAction.ACTION_PATH;
    }

    @Override
    protected Identifiable getIdentifiable() {
        return RelationsGroupSecure.INSTANCE;
    }
}
