package ru.runa.wfe.var.matcher;

import java.util.List;

import ru.runa.wfe.var.VariableTypeMatcher;
import ru.runa.wfe.var.file.IFileVariable;

public class FileVariableMatcher implements VariableTypeMatcher {

    @Override
    public boolean matches(Object value) {
        if (IFileVariable.class.isAssignableFrom(value.getClass())) {
            return true;
        }
        return isFileOrListOfFiles(value);
    }

    public static boolean isFileOrListOfFiles(Object value) {
        if (value instanceof List) {
            for (Object object : (List<Object>) value) {
                if (object != null) {
                    // decide by first not-null value
                    return object instanceof IFileVariable;
                }
            }
            // match empty list too in order to prevent filling in to
            // serializable converter in next steps
            return true;
        }
        return false;
    }
}
