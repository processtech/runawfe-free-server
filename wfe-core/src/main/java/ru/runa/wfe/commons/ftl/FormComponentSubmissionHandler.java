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

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Interface allows form components make custom parsing of user input.
 *
 * @author dofs
 * @since 4.2.0
 */
public interface FormComponentSubmissionHandler {

    /**
     * Processing method
     *
     * @param interaction
     *            task form interaction
     * @param variableDefinition
     *            variable definition
     * @param userInput
     *            raw user input
     * @param errors
     *            map containing field errors (messages will be displayed to user)
     *
     * @return parsed values
     * @throws Exception
     *             if any error occurs; message will be displayed to user
     */
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> errors) throws Exception;

}
