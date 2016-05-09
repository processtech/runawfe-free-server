package ru.runa.common.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class HierarchyTypeSelectUtils {
    private static final String TYPE_TYPE = "type";
    private static final String TYPE_SEL = "typeSel";
    private static final String TYPE_ATTRIBUTES = "TypeAttributes";
    private static final String TYPE_DEFAULT = "_default_type_";

    public static TD createHierarchyTypeSelectTD(Iterator<String[]> typesIterator, String[] entityType, PageContext pageContext) {
        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute(TYPE_ATTRIBUTES);
        String selectedValue = attr != null ? attr.get(TYPE_SEL) : entityType == null ? TYPE_DEFAULT : null;
        String newTypeName = attr != null ? attr.get(TYPE_TYPE) : "";

        TD td = new TD();
        Select select = getHierarchyTypeSelectElement(typesIterator, selectedValue, entityType, pageContext);
        td.addElement(select);
        td.addElement(Entities.NBSP);
        Input typeInput = new Input(Input.TEXT, TYPE_TYPE, newTypeName == null ? "" : String.valueOf(newTypeName));
        typeInput.setID("newHierarchyTypeName");
        typeInput.setStyle("width: 300px;");
        if (!TYPE_DEFAULT.equals(selectedValue)) {
            typeInput.setDisabled(true);
        } else {
            typeInput.setClass(Resources.CLASS_REQUIRED);
        }
        td.addElement(typeInput);
        return td;
    }

    /**
     * Extracts from request selected type.
     * 
     * @param request
     *            Request, processing by server.
     * @param allowEmpty
     *            Flag, equals true, if empty (no) type selection is allowed and false otherwise (exception will be thrown).
     * @return Returns full selected type.
     */
    public static List<String> extractSelectedType(HttpServletRequest request) {
        List<String> fullType;
        String paramType = request.getParameter(HierarchyTypeSelectUtils.TYPE_TYPE);
        String paramTypeSelected = request.getParameter(HierarchyTypeSelectUtils.TYPE_SEL);
        saveTypeAsAttribute(request, paramType, paramTypeSelected);
        if (paramTypeSelected == null || paramTypeSelected.equals(HierarchyTypeSelectUtils.TYPE_DEFAULT)) {
            if (paramType == null) {
                paramType = "";
            }
            fullType = Lists.newArrayList(paramType);
        } else {
            fullType = Lists.newArrayList(Splitter.on('/').omitEmptyStrings().split(paramTypeSelected));
            if (!Strings.isNullOrEmpty(paramType)) {
                fullType.add(paramType);
            }
        }
        return fullType;
    }

    public static void saveTypeAsAttribute(ServletRequest request, String paramType, String paramTypeSelected) {
        Map<String, String> typeParamsHolder = new HashMap<String, String>();
        typeParamsHolder.put(HierarchyTypeSelectUtils.TYPE_TYPE, paramType);
        typeParamsHolder.put(HierarchyTypeSelectUtils.TYPE_SEL, paramTypeSelected);
        request.setAttribute(HierarchyTypeSelectUtils.TYPE_ATTRIBUTES, typeParamsHolder);
    }

    public static boolean isEmptyType(List<String> type) {
        return type == null || type.size() == 0 || (type.size() == 1 && Strings.isNullOrEmpty(type.get(0)));
    }

    static Select getHierarchyTypeSelectElement(Iterator<String[]> typesIterator, String selectedValue, String[] entityType, PageContext pageContext) {
        Select select = new Select(TYPE_SEL);
        select.setID("hierarchyTypeSelect");
        select.addElement(createOption(MessagesCommon.NO_TYPE_SELECTED.message(pageContext), TYPE_DEFAULT, TYPE_DEFAULT.equals(selectedValue)));
        while (typesIterator.hasNext()) {
            String[] type = typesIterator.next();

            StringBuilder typeBuild = new StringBuilder();
            StringBuilder fullTypeBuild = new StringBuilder();
            for (int i = 1; i < type.length; ++i) {
                typeBuild.append(Entities.NBSP).append(Entities.NBSP).append(Entities.NBSP);
                fullTypeBuild.append(type[i - 1]).append("/");
            }
            typeBuild.append(type[type.length - 1]);
            fullTypeBuild.append(type[type.length - 1]);
            String fullType = fullTypeBuild.toString();
            boolean selected = (selectedValue == null && Arrays.equals(type, entityType)) || fullType.equals(selectedValue);
            select.addElement(createOption(typeBuild.toString(), fullType, selected));
        }
        return select;
    }

    private static Option createOption(String labelMessage, String value, boolean selected) {
        Option option = new Option();
        option.addElement(labelMessage);
        option.setValue(value);
        if (selected) {
            option.setSelected(selected);
        }
        return option;
    }
}
