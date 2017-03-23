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
 */package ru.runa.wfe.commons.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONAware;

import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public abstract class JsonAjaxCommand implements AjaxCommand {
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void execute(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        JSONAware json = execute(user, request);
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    protected abstract JSONAware execute(User user, HttpServletRequest request) throws Exception;

}
