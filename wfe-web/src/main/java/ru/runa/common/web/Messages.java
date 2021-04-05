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
package ru.runa.common.web;

import javax.servlet.jsp.PageContext;
import org.apache.struts.util.MessageResources;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.FieldDescriptor;

/**
 * Created 14.05.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class Messages {

    private Messages() {
    }

    public static String getMessage(String key, PageContext pageContext) {
        String value = Commons.getMessage(key, pageContext);
        if (value == null) {
            value = '!' + key + '!';
        }
        return value;
    }

    public static String getMessage(ClassPresentationType classPresentationType, String fieldName, PageContext pageContext) {
        String key = getClassPresentationFieldNameKey(classPresentationType, fieldName);
        return getMessage(key, pageContext);
    }

    public static String getMessage(BatchPresentation batchPresentation, FieldDescriptor fieldDescriptor, PageContext pageContext) {
        if (fieldDescriptor.variableValue != null) {
            return fieldDescriptor.variableValue;
        }
        String key = getClassPresentationFieldNameKey(batchPresentation.getType(), fieldDescriptor.name);
        return getMessage(key, pageContext);
    }

    public static String getMessage(BatchPresentation batchPresentation, FieldDescriptor fieldDescriptor, MessageResources messageResources) {
        if (fieldDescriptor.variableValue != null) {
            return fieldDescriptor.variableValue;
        }
        String key = getClassPresentationFieldNameKey(batchPresentation.getType(), fieldDescriptor.name);
        return messageResources.getMessage(key);
    }

    private static String getClassPresentationFieldNameKey(ClassPresentationType classPresentationType, String fieldName) {
        return "batch_presentation." + classPresentationType.getLocalizationKey() + "." + fieldName;
    }
}
