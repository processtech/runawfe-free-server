package ru.runa.common.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

import ru.runa.wfe.commons.Utils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class CategoriesSelectUtils {
    private static final String TYPE_TYPE = "type";
    private static final String TYPE_SEL = "typeSel";
    private static final String TYPE_ATTRIBUTES = "TypeAttributes";
    private static final String TYPE_DEFAULT = "_default_type_";

    public static TD createSelectTD(Iterator<String[]> iterator, String[] entityType, PageContext pageContext) {
        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute(TYPE_ATTRIBUTES);
        String selectedValue = attr != null ? attr.get(TYPE_SEL) : entityType == null ? TYPE_DEFAULT : null;
        String newTypeName = attr != null ? attr.get(TYPE_TYPE) : "";

        TD td = new TD();
        Select select = getSelectElement(iterator, selectedValue, entityType, pageContext);
        td.addElement(select);
        td.addElement(Entities.NBSP);
        Input typeInput = new Input(Input.TEXT, TYPE_TYPE, newTypeName == null ? "" : String.valueOf(newTypeName));
        typeInput.setID("newHierarchyTypeName");
        typeInput.setStyle("width: 300px;");
        if (!TYPE_DEFAULT.equals(selectedValue)) {
            typeInput.setDisabled(true);
        }
        td.addElement(typeInput);
        return td;
    }

    /**
     * Extracts from request selected categories.
     *
     * @param request
     *            Request, processing by server.
     * @param allowEmpty
     *            Flag, equals true, if empty (no) type selection is allowed and false otherwise (exception will be thrown).
     * @return Returns full selected type.
     */
    public static List<String> extract(ServletRequest request) {
        List<String> fullType;
        String paramType = request.getParameter(CategoriesSelectUtils.TYPE_TYPE);
        String paramTypeSelected = request.getParameter(CategoriesSelectUtils.TYPE_SEL);
        saveAsAttribute(request, paramType, paramTypeSelected);
        if (paramTypeSelected == null || paramTypeSelected.equals(CategoriesSelectUtils.TYPE_DEFAULT)) {
            if (paramType == null) {
                paramType = "";
            }
            fullType = Lists.newArrayList(paramType);
        } else {
            fullType = Lists.newArrayList(Splitter.on(Utils.CATEGORY_DELIMITER).omitEmptyStrings().split(paramTypeSelected));
            if (!Strings.isNullOrEmpty(paramType)) {
                fullType.add(paramType);
            }
        }
        return fullType;
    }

    public static void saveAsAttribute(ServletRequest request, String paramType, String paramTypeSelected) {
        Map<String, String> typeParamsHolder = new HashMap<String, String>();
        typeParamsHolder.put(CategoriesSelectUtils.TYPE_TYPE, paramType);
        typeParamsHolder.put(CategoriesSelectUtils.TYPE_SEL, paramTypeSelected);
        request.setAttribute(CategoriesSelectUtils.TYPE_ATTRIBUTES, typeParamsHolder);
    }

    static Select getSelectElement(Iterator<String[]> typesIterator, String selectedValue, String[] entityType, PageContext pageContext) {
        Select select = new Select(TYPE_SEL);
        select.setID("hierarchyTypeSelect");
        select.addElement(HTMLUtils.createOption(TYPE_DEFAULT, MessagesCommon.NO_TYPE_SELECTED.message(pageContext),
                TYPE_DEFAULT.equals(selectedValue)));
        while (typesIterator.hasNext()) {
            String[] type = typesIterator.next();

            StringBuilder typeBuild = new StringBuilder();
            StringBuilder fullTypeBuild = new StringBuilder();
            for (int i = 1; i < type.length; ++i) {
                typeBuild.append(Entities.NBSP).append(Entities.NBSP).append(Entities.NBSP);
                fullTypeBuild.append(type[i - 1]).append(Utils.CATEGORY_DELIMITER);
            }
            typeBuild.append(type[type.length - 1]);
            fullTypeBuild.append(type[type.length - 1]);
            String fullType = fullTypeBuild.toString();
            boolean selected = selectedValue == null && Arrays.equals(type, entityType) || fullType.equals(selectedValue);
            select.addElement(HTMLUtils.createOption(fullType, typeBuild.toString(), selected));
        }
        return select;
    }
}
