package ru.runa.wf.web.ftl.component;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONAware;

import ru.runa.wfe.commons.ftl.AjaxJsonFormComponent;

public class TreeviewSupport extends AjaxJsonFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        return null;
    }

    @Override
    protected String renderRequest() throws Exception {
        StringBuffer html = new StringBuffer();
        if (webHelper != null) {
            html.append("<link rel='stylesheet' href='" + webHelper.getUrl("/css/jquery.treeview.css") + "'>\n");
            html.append("<script type='text/javascript' src='" + webHelper.getUrl("/js/jquery.treeview.js") + "'>c=0;</script>\n");
        }
        return html.toString();
    }

}
