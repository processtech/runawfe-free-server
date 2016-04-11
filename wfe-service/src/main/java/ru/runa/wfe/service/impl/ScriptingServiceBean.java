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
package ru.runa.wfe.service.impl;

import groovy.lang.GroovyShell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.AdminScriptRunner;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.service.interceptors.CacheReloader;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.User;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, CacheReloader.class, PerformanceObserver.class, EjbTransactionSupport.class,
        SpringBeanAutowiringInterceptor.class })
@WebService(name = "ScriptingAPI", serviceName = "ScriptingWebService")
@SOAPBinding
public class ScriptingServiceBean implements ScriptingService {
    @Autowired
    private AdminScriptRunner runner;

    @Override
    @WebResult(name = "result")
    public void executeAdminScript(@WebParam(name = "user") User user, @WebParam(name = "configData") byte[] configData,
            @WebParam(name = "processDefinitionsBytes") byte[][] processDefinitionsBytes) {
        runner.setUser(user);
        runner.setProcessDefinitionsBytes(processDefinitionsBytes);
        runner.runScript(configData);
    }

    @Override
    @WebMethod(exclude = true)
    public List<String> executeAdminScriptSkipError(User user, byte[] configData, byte[][] processDefinitionsBytes, Map<String, byte[]> configs,
            String defaultPasswordValue) {
        runner.setUser(user);
        runner.setProcessDefinitionsBytes(processDefinitionsBytes);
        runner.setConfigs(configs);
        runner.setDefaultPasswordValue(defaultPasswordValue);
        runner.init();

        Document document = XmlUtils.parseWithXSDValidation(configData, "workflowScript.xsd");
        Element scriptElement = document.getRootElement();
        List<Element> elements = scriptElement.elements();
        List<String> errors = new ArrayList<String>();
        for (Element element : elements) {
            try {
                runner.handleElement(element);
            } catch (AdminScriptException e) {
                if (!(e.getCause() instanceof ExecutorAlreadyExistsException)) {
                    errors.add(e.getMessage());
                }
            }
        }

        runner.setConfigs(null);
        runner.setDefaultPasswordValue(null);
        return errors;
    }

    @Override
    @WebResult(name = "result")
    public void executeGroovyScript(@WebParam(name = "user") User user, @WebParam(name = "script") String script) {
        if (!SystemProperties.isExecuteGroovyScriptInAPIEnabled()) {
            throw new ConfigurationException(
                    "In order to enable script execution set property 'scripting.groovy.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        GroovyShell shell = new GroovyShell();
        shell.evaluate(script);
    }
}
