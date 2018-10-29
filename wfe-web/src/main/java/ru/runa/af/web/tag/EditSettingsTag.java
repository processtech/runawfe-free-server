package ru.runa.af.web.tag;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.apache.struts.Globals;
import org.apache.struts.taglib.TagUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.SaveSettingsAction;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.form.SettingsFileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author: petrmikheev Date: 26.08.2012
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "editSettings")
@CommonsLog
public class EditSettingsTag extends TitledFormTag {
    private static final long serialVersionUID = -426375016105456L;

    private static class Setting {
        public String title;
        public String pattern = null;
        public List<String> values = new LinkedList<>();

        public Setting(String title) {
            this.title = title;
        }
    }

    private static class SettingsFile {
        Setting defaultSetting = new Setting(null);
        List<Setting> settings = new ArrayList<>();
    }

    public static final TreeMap<String, SettingsFile> settingsList;

    static {
        settingsList = readSettingsList("settingsList.xml");
    }

    @SuppressWarnings("unchecked")
    private static void parseSettingType(Setting p, Element el) {
        p.pattern = el.attributeValue("pattern");
        List<Element> vlist = el.elements();
        for (Element v : vlist) {
            if (v.getName() != "value") {
                continue;
            }
            p.values.add(v.getText());
        }
    }

    @SuppressWarnings("unchecked")
    private static TreeMap<String, SettingsFile> readSettingsList(String path) {
        TreeMap<String, SettingsFile> result = new TreeMap<>();
        try (InputStream is = ClassLoaderUtil.getAsStreamNotNull(path, EditSettingsTag.class)) {
            Document document = XmlUtils.parseWithoutValidation(is);
            List<Element> files = document.getRootElement().elements();
            for (Element f : files) {
                SettingsFile pf = new SettingsFile();
                result.put(f.attributeValue("title"), pf);
                parseSettingType(pf.defaultSetting, f);
                List<Element> plist = f.elements();
                for (Element p : plist) {
                    if (p.getName() != "property") {
                        continue;
                    }
                    Setting np = new Setting(p.attributeValue("title"));
                    parseSettingType(np, p);
                    pf.settings.add(np);
                }
            }
        } catch (Exception e) {
            log.error("Can`t parse " + path, e);
        }
        return result;
    }

    private String resource;

    public String getResource() {
        return resource;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_SAVE.message(pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        if (!Delegates.getExecutorService().isAdministrator(getUser())) {
            throw new AuthorizationException("No permission on this page");
        }
        if (!settingsList.containsKey(resource)) {
            throw new IllegalArgumentException();
        }
        getForm().setMethod(Form.POST);
        tdFormElement.addElement(new Input(Input.hidden, SettingsFileForm.RESOURCE_INPUT_NAME, resource));
        tdFormElement.addElement(new Input(Input.hidden, "saveButtonText", MessagesCommon.BUTTON_SAVE.message(pageContext)));
        SettingsFile pf = settingsList.get(resource);
        PropertyResources properties = new PropertyResources(resource);
        Table table = new Table();
        table.setClass("list");
        String headerTitle = MessagesOther.LABEL_SETTING_TITLE.message(pageContext);
        String headerDescription = MessagesOther.LABEL_SETTING_DESCRIPTION.message(pageContext);
        String headerValue = MessagesOther.LABEL_SETTING_VALUE.message(pageContext);
        table.addElement("<tr><th class='list'>" + headerTitle + "</th><th class='list'>" + headerDescription
                + "</th><th class='list' style='width:300px'>" + headerValue + "</th></tr>");
        List<Setting> lp = pf.settings;
        if (lp.size() == 0) {
            lp = new LinkedList<Setting>();
            for (Object k : properties.getAllPropertyNames()) {
                Setting p = new Setting(k.toString());
                p.pattern = pf.defaultSetting.pattern;
                p.values = pf.defaultSetting.values;
                lp.add(p);
            }
        }
        for (Setting p : lp) {
            String description = getDescription(pageContext, resource + "_" + p.title);
            String value = properties.getStringProperty(p.title, "");
            Input oldInput = new Input(Input.hidden, SettingsFileForm.oldValueInputName(p.title), value);
            String input;
            if (p.values.size() == 0) {
                Input i = new Input(Input.text, SettingsFileForm.newValueInputName(p.title), value);
                i.addAttribute("style", "width: 290px");
                if (p.pattern != null) {
                    i.addAttribute("pattern", p.pattern);
                }
                input = i.toString();
            } else {
                StringBuilder b = new StringBuilder();
                b.append("<select style='width: 300px' name='" + SettingsFileForm.newValueInputName(p.title) + "'>");
                b.append("<option selected>");
                b.append(value);
                b.append("</option>");
                for (String v : p.values) {
                    if (v.equals(value)) {
                        continue;
                    }
                    b.append("<option>");
                    b.append(v);
                    b.append("</option>");
                }
                b.append("</select>");
                input = b.toString();
            }
            table.addElement("<tr><td class='list'>" + p.title + "</td>" + "<td class='list'>" + description + "</td>" + "<td class='list'>"
                    + oldInput.toString() + input + "</td>" + "</tr>");
        }
        tdFormElement.addElement(table);
    }

    public static String getDescription(PageContext pageContext, String key) {
        try {
            String res = TagUtils.getInstance().message(pageContext, "settingsDescriptions", Globals.LOCALE_KEY, key);
            if (res == null) {
                res = "";
            }
            return res;
        } catch (JspException e) {
            log.error("Error at getDescription()", e);
            return key;
        }
    }

    @Override
    protected String getTitle() {
        return getDescription(pageContext, resource);
    }

    @Override
    public String getAction() {
        return SaveSettingsAction.SAVE_SETTINGS_ACTION_PATH;
    }

    @Override
    protected boolean isCancelButtonEnabled() {
        return true;
    }

    @Override
    protected String getCancelButtonAction() {
        return "manage_settings.do";
    }

}
