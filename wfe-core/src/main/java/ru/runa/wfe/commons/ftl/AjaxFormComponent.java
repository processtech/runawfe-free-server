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
package ru.runa.wfe.commons.ftl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Form component which allows user interaction with server through ajax requests.
 *
 * @author dofs
 *
 */
public abstract class AjaxFormComponent extends FormComponent {
    private static final long serialVersionUID = 1L;
    public static final String COMPONENT_SESSION_PREFIX = "ajax_form_component_";

    /**
     * Invoked on ajax request
     */
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    @Override
    protected String exportScript(Map<String, String> substitutions, boolean globalScope) {
        addJsonUrlSubstitution(substitutions);
        return super.exportScript(substitutions, globalScope);
    }

    @Override
    protected String exportScript(Map<String, String> substitutions, boolean globalScope, String name) {
        addJsonUrlSubstitution(substitutions);
        return super.exportScript(substitutions, globalScope, name);
    }

    private void addJsonUrlSubstitution(Map<String, String> substitutions) {
        substitutions.put("JSON_URL", webHelper.getUrl("/form.fp?component=" + getName() + "&qualifier=" + getVariableNameForSubmissionProcessing()));
    }
}
