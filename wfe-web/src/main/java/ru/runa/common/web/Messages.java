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
