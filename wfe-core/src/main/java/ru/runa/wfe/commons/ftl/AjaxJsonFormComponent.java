package ru.runa.wfe.commons.ftl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONAware;

import com.google.common.base.Charsets;

public abstract class AjaxJsonFormComponent extends AjaxFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    public final void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONAware json = processAjaxRequest(request);
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    protected abstract JSONAware processAjaxRequest(HttpServletRequest request) throws Exception;

}
