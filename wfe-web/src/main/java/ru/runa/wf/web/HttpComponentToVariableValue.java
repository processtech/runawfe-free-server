package ru.runa.wf.web;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.upload.FormFile;

import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.user.ExecutorLoader;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;

import com.google.common.base.Throwables;

/**
 * Try to convert simple object to variable value.
 */
public class HttpComponentToVariableValue implements VariableFormatVisitor<Object, HttpComponentToVariableValueContext> {
    private static final Log log = LogFactory.getLog(HttpComponentToVariableValue.class);

    /**
     * Component for loading executors.
     */
    private final ExecutorLoader executorLoader;

    /**
     * Errors would be stored there. Map from field name to error description.
     */
    private final Map<String, String> errors;

    public HttpComponentToVariableValue(ExecutorLoader executorLoader, Map<String, String> errors) {
        this.executorLoader = executorLoader;
        this.errors = errors;
    }

    @Override
    public Object onDate(DateFormat dateFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(dateFormat, context);
    }

    @Override
    public Object onTime(TimeFormat timeFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(timeFormat, context);
    }

    @Override
    public Object onDateTime(DateTimeFormat dateTimeFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(dateTimeFormat, context);
    }

    @Override
    public Object onExecutor(ExecutorFormat executorFormat, HttpComponentToVariableValueContext context) {
        if (context.value == null) {
            return null;
        }
        String stringValue = context.getStringValueToFormat();
        try {
            return TypeConversionUtil.convertToExecutor(stringValue, executorLoader);
        } catch (Exception e) {
            saveErrorAndContinue(context, stringValue, e);
        }
        return null;
    }

    @Override
    public Object onBoolean(BooleanFormat booleanFormat, HttpComponentToVariableValueContext context) {
        Object value = context.value;
        if (value == null) {
            // HTTP FORM doesn't pass unchecked checkbox value
            return Boolean.FALSE;
        }
        return convertDefault(booleanFormat, context);
    }

    @Override
    public Object onBigDecimal(BigDecimalFormat bigDecimalFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(bigDecimalFormat, context);
    }

    @Override
    public Object onDouble(DoubleFormat doubleFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(doubleFormat, context);
    }

    @Override
    public Object onLong(LongFormat longFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(longFormat, context);
    }

    @Override
    public Object onFile(FileFormat fileFormat, HttpComponentToVariableValueContext context) {
        if (context.value instanceof FormFile) {
            FormFile formFile = (FormFile) context.value;
            if (formFile.getFileSize() > 0) {
                String contentType = formFile.getContentType();
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                try {
                    return new FileVariableImpl(formFile.getFileName(), formFile.getFileData(), contentType);
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        } else if (context.value instanceof UploadedFile) {
            UploadedFile uploadedFile = (UploadedFile) context.value;
            if (uploadedFile.getFileVariable() instanceof FileVariableProxy) {
                // for process update value
                return uploadedFile.getFileVariable();
            }
            else if (uploadedFile.getFileVariable() instanceof FileVariableImpl) {
                return uploadedFile.getFileVariable();
            }

            if (uploadedFile.getContent() == null) {
                throw new InternalApplicationException("No content submitted for " + uploadedFile);
            }
            return new FileVariableImpl(uploadedFile.getName(), uploadedFile.getContent(), uploadedFile.getMimeType());
        }
        return null;
    }

    @Override
    public Object onHidden(HiddenFormat hiddenFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(hiddenFormat, context);
    }

    @Override
    public Object onList(ListFormat listFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(listFormat, context);
    }

    @Override
    public Object onMap(MapFormat mapFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(mapFormat, context);
    }

    @Override
    public Object onProcessId(ProcessIdFormat processIdFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(processIdFormat, context);
    }

    @Override
    public Object onString(StringFormat stringFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(stringFormat, context);
    }

    @Override
    public Object onTextString(TextFormat textFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(textFormat, context);
    }

    @Override
    public Object onFormattedTextString(FormattedTextFormat textFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(textFormat, context);
    }

    @Override
    public Object onUserType(UserTypeFormat userTypeFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(userTypeFormat, context);
    }

    @Override
    public Object onOther(VariableFormat variableFormat, HttpComponentToVariableValueContext context) {
        return convertDefault(variableFormat, context);
    }

    /**
     * Default conversation implementation: assume value is String and try to parse it.
     * 
     * @param format
     *            Variable format.
     * @param context
     *            Operation context.
     * @return Returns variable value.
     */
    private Object convertDefault(VariableFormat format, HttpComponentToVariableValueContext context) {
        if (context.value == null) {
            return null;
        }
        String stringValue = context.getStringValueToFormat();
        try {
            return format.parse(stringValue);
        } catch (Exception e) {
            saveErrorAndContinue(context, stringValue, e);
        }
        return null;
    }

    /**
     * Save exception in errors if required and continue execution.
     * 
     * @param context
     *            Operation context.
     * @param valueToFormat
     *            Converting value, which raises exception.
     * @param e
     *            Exception, occurred on value conversation.
     */
    private void saveErrorAndContinue(HttpComponentToVariableValueContext context, final String valueToFormat, Exception e) {
        if (valueToFormat.length() > 0) {
            log.warn(e);
            // in other case we put validation in logic
            errors.put(context.variableName, e.getMessage());
        } else {
            log.debug(e);
        }
    }
}
