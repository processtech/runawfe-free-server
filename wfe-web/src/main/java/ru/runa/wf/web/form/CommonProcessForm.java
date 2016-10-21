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
package ru.runa.wf.web.form;

import ru.runa.common.web.form.IdForm;

/**
 * @struts:form name = "commonProcessForm"
 */
public class CommonProcessForm extends IdForm {
    private static final long serialVersionUID = -4345644809568995336L;

    private String submitButton;

    private boolean multipleSubmit;

    public String getSubmitButton() {
        if (multipleSubmit) {
            return submitButton;
        }
        return null;
    }

    public void setSubmitButton(String submitButton) {
        this.submitButton = submitButton;
    }

    public void setMultipleSubmit(boolean multipleSubmit) {
        this.multipleSubmit = multipleSubmit;
    }
}
