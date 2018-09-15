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
package ru.runa.wf.web.customtag;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * @since 1.0
 * @deprecated Use Freemarker forms
 */
@Deprecated
public interface VarTag {
    String getHtml(User user, String varName, Object varValue, PageContext pageContext, VariableProvider variableProvider) throws Exception;
}
