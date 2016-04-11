package ru.runa.wfe.var.format;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ExecutorFormat extends VariableFormat implements VariableDisplaySupport {
    private static Map<Class<?>, String> tooltipTemplates = Maps.newHashMap();
    static {
        tooltipTemplates.put(Executor.class, ClassLoaderUtil.getAsString(Executor.class.getName() + ".tooltip.template", ExecutorFormat.class));
        tooltipTemplates.put(Actor.class, ClassLoaderUtil.getAsString(Actor.class.getName() + ".tooltip.template", ExecutorFormat.class));
        tooltipTemplates.put(Group.class, ClassLoaderUtil.getAsString(Group.class.getName() + ".tooltip.template", ExecutorFormat.class));
    }

    @Override
    public Class<? extends Executor> getJavaClass() {
        return Executor.class;
    }

    @Override
    public String getName() {
        return "executor";
    }

    @Override
    protected Executor convertFromStringValue(String source) {
        return TypeConversionUtil.convertTo(Executor.class, source);
    }

    @Override
    protected String convertToStringValue(Object object) {
        return ((Executor) object).getName();
    }

    @Override
    protected Object convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        if (object.containsKey("name")) {
            return TypeConversionUtil.convertTo(Executor.class, object.get("name"));
        }
        if (object.containsKey("id")) {
            return TypeConversionUtil.convertTo(Executor.class, "ID" + object.get("id"));
        }
        throw new InternalApplicationException("Neither 'id' or 'name' attribute found in " + object);
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        Executor executor = (Executor) value;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", executor.getId());
        jsonObject.put("name", executor.getName());
        jsonObject.put("fullName", executor.getFullName());
        return jsonObject;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        Executor executor = (Executor) object;
        boolean link = false;
        try {
            link = webHelper.useLinkForExecutor(user, executor);
        } catch (Exception e) {
            LogFactory.getLog(getClass()).warn("Unable to determine whether useLinkForExecutor", e);
        }
        if (link) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put(WebHelper.PARAM_ID, executor.getId());
            String href = webHelper.getActionUrl(WebHelper.ACTION_VIEW_EXECUTOR, params);
            String html = "<a href=\"" + href + "\"";
            String tooltipTemplate = tooltipTemplates.get(executor.getClass());
            if (!Strings.isNullOrEmpty(tooltipTemplate)) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("object", object);
                IVariableProvider variableProvider = new MapVariableProvider(map);
                String title = ExpressionEvaluator.process(user, tooltipTemplate, variableProvider, webHelper);
                html += " title=\"" + title + "\"";
            }
            html += ">" + executor.getLabel() + "</a>";
            return html;
        } else {
            return executor.getLabel();
        }
    }
}
