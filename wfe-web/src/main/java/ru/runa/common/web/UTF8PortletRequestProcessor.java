package ru.runa.common.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.portals.bridges.struts.PortletRequestProcessor;

import com.google.common.base.Charsets;

public class UTF8PortletRequestProcessor extends PortletRequestProcessor {
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding(Charsets.UTF_8.name());
        super.process(request, response);
    }
}
