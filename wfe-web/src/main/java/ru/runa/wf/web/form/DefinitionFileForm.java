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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.definition.IFileDataProvider;

/**
 * @struts:form name = "idUrlForm"
 */
public class DefinitionFileForm extends IdForm {
    private static final long serialVersionUID = 1L;

    public static final String URL_INPUT_NAME = "fileName";

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String url) {
        fileName = url;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        if (fileName == null) {
            fileName = IFileDataProvider.INDEX_FILE_NAME;
        }
        return super.validate(mapping, request);
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        fileName = null;
    }
}
