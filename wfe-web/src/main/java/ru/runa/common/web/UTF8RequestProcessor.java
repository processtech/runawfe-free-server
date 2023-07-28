package ru.runa.common.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.RequestProcessor;

import com.google.common.base.Charsets;

/**
 * Created on 15.09.2004
 * 
 */
public class UTF8RequestProcessor extends RequestProcessor {

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            request.setCharacterEncoding(Charsets.UTF_8.name());
            super.process(request, response);
        } catch (UnsupportedEncodingException e) {
            // will never happened
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
