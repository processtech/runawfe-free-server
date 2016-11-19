/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
