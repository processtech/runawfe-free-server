package ru.runa.wfe.var.format;

import com.google.common.collect.Maps;
import java.util.HashMap;
import javax.xml.bind.DatatypeConverter;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.file.LinkedWithProcessDefinition;

/**
 * This class is marker class for validation.
 */
public class FileFormat extends VariableFormat implements VariableDisplaySupport {

    @Override
    public Class<? extends FileVariable> getJavaClass() {
        return FileVariable.class;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String convertToStringValue(Object object) {
        return ((FileVariable) object).getName();
    }

    @Override
    public FileVariable convertFromStringValue(String string) {
        return (FileVariable) convertFromJSONValue(JSONValue.parse(string.replaceAll("&quot;", "\"")));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object convertToJSONValue(Object value) {
        FileVariable fileVariable = (FileVariable) value;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", fileVariable.getName());
        jsonObject.put("contentType", fileVariable.getContentType());
        jsonObject.put("data", DatatypeConverter.printBase64Binary(fileVariable.getData()));
        return jsonObject;
    }

    @Override
    protected Object convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        String fileName = (String) object.get("fileName");
        if (fileName == null) {
            throw new InternalApplicationException("Attribute 'fileName' is not set in " + object);
        }
        String contentType = (String) object.get("contentType");
        if (contentType == null) {
            throw new InternalApplicationException("Attribute 'contentType' is not set in " + object);
        }
        String data = (String) object.get("data");
        if (data == null) {
            throw new InternalApplicationException("Attribute 'data' is not set in " + object);
        }
        return new FileVariableImpl(fileName, DatatypeConverter.parseBase64Binary(data), contentType);
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        FileVariable value = (FileVariable) object;
        if (value == null || value.getName() == null) {
            return "";
        }
        HashMap<String, Object> params = new HashMap<>(4);
        params.put(WebHelper.PARAM_ID, processId);
        params.put(WebHelper.PARAM_VARIABLE_NAME, name);
        if (value instanceof LinkedWithProcessDefinition) {
            params.put(WebHelper.PARAM_DEFINITION_ID, ((LinkedWithProcessDefinition) value).getDefinitionId());
        }

        String href = webHelper.getActionUrl(WebHelper.ACTION_DOWNLOAD_PROCESS_FILE, params);
        return "<a href=\"" + href + "\">" + value.getName() + "</a>";
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onFile(this, context);
    }

}
