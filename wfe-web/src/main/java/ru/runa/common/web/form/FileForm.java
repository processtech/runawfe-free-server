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
package ru.runa.common.web.form;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

import ru.runa.common.web.MessagesException;

/**
 * Created on 06.10.2004
 * 
 * @struts:form name = "fileForm"
 */
public class FileForm extends IdForm {

    private static final long serialVersionUID = 7850320221673917388L;

    public static final String FILE_INPUT_NAME = "file";

    private FormFile file;

    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        try {
            if (file == null || file.getFileData() == null || file.getFileData().length < 1) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
            }
        } catch (FileNotFoundException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        } catch (IOException e) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
