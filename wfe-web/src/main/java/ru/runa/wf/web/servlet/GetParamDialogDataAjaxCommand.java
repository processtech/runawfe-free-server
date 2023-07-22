package ru.runa.wf.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.User;

@SuppressWarnings("unchecked")
public class GetParamDialogDataAjaxCommand extends JsonAjaxCommand {

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String rendererClassName = request.getParameter("renderer");
        ParamRenderer renderer = ClassLoaderUtil.instantiate(rendererClassName);
        List<String[]> data = renderer.loadJSEditorData(user);
        JSONArray array = new JSONArray();
        for (int i = 0; i < data.size(); i++) {
            String[] strings = data.get(i);
            array.add(createJsonObject(strings[0], strings[1]));
        }
        return array;
    }

    private JSONObject createJsonObject(String value, String text) {
        JSONObject object = new JSONObject();
        object.put("value", value);
        object.put("text", text);
        return object;
    }

    private String normalize(String data) {
        if (data.contains("\"")) {
            return data.replaceAll("\"", "'");
        }
        return data;
    }

}
