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
package ru.runa.wfe.service.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.utils.AdminScriptUtils;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Created on 12.12.2005
 *
 */
public class AdminScriptClient {
    private static final String DEPLOY_PROCESS_DEFINITION_TAG_NAME = "deployProcessDefinition";
    private static final String FILE_ATTRIBUTE_NAME = "file";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: AdminScriptRunner <scriptpath> <username> <password>");
            System.out.println("Example: AdminScriptRunner /home/foo/wfescript.xml foo $eCreTw0rd");
            System.exit(-1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Config file " + args[0] + " does not exist");
            System.exit(-1);
        }
        try {
            byte[] scriptBytes = Files.toByteArray(file);
            User user = Delegates.getAuthenticationService().authenticateByLoginPassword(args[1], args[2]);
            run(user, scriptBytes, new Handler() {

                @Override
                public void onTransactionException(Exception e) {
                    System.out.println(e.getMessage());
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void run(User user, byte[] scriptBytes, Handler handler) throws IOException {
        InputStream scriptInputStream = new ByteArrayInputStream(scriptBytes);
        Document allDocument = XmlUtils.parseWithXSDValidation(scriptInputStream, "workflowScript.xsd");
        Element root = allDocument.getRootElement();
        List<Element> transactionScopeElements = root.elements("transactionScope");
        String defaultTransactionScope = root.attributeValue("defaultTransactionScope");
        if (transactionScopeElements.size() == 0 && "all".equals(defaultTransactionScope)) {
            byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(root);
            Delegates.getScriptingService().executeAdminScript(user, scriptBytes, processDefinitionsBytes);
        } else {
            if (transactionScopeElements.size() > 0) {
                System.out.println("multiple docs [by <transactionScope>]: " + transactionScopeElements.size());
                for (Element transactionScopeElement : transactionScopeElements) {
                    Document document = AdminScriptUtils.createScriptDocument();
                    List<Element> children = transactionScopeElement.elements();
                    for (Element element : children) {
                        document.getRootElement().add(element.createCopy());
                    }
                    byte[] bs = XmlUtils.save(document);
                    byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(document.getRootElement());
                    try {
                        handler.onStartTransaction(bs);
                        Delegates.getScriptingService().executeAdminScript(user, bs, processDefinitionsBytes);
                        handler.onEndTransaction();
                    } catch (Exception e) {
                        handler.onTransactionException(e);
                    }
                }
            } else {
                List<Element> allChildrenElements = allDocument.getRootElement().elements();
                System.out.println("multiple docs [by defaultTransactionScope]: " + allChildrenElements.size());
                for (Element child : allChildrenElements) {
                    Document document = AdminScriptUtils.createScriptDocument();
                    document.getRootElement().add(child.createCopy());
                    byte[] bs = XmlUtils.save(document);
                    byte[][] processDefinitionsBytes = readProcessDefinitionsToByteArrays(document.getRootElement());
                    try {
                        handler.onStartTransaction(bs);
                        Delegates.getScriptingService().executeAdminScript(user, bs, processDefinitionsBytes);
                        handler.onEndTransaction();
                    } catch (Exception e) {
                        handler.onTransactionException(e);
                    }
                }
            }
        }
    }

    private static byte[][] readProcessDefinitionsToByteArrays(Element element) throws IOException {
        String[] fileNames = readProcessDefinitionFileNames(element);
        byte[][] processDefinitionsBytes = new byte[fileNames.length][];
        for (int i = 0; i < fileNames.length; i++) {
            File processFile = new File(fileNames[i]);
            if (processFile.isFile()) {
                processDefinitionsBytes[i] = Files.toByteArray(new File(fileNames[i]));
            } else {
                processDefinitionsBytes[i] = IOCommons.jarToBytesArray(processFile);
            }
        }
        return processDefinitionsBytes;
    }

    private static String[] readProcessDefinitionFileNames(Element element) {
        List<Element> elements = element.elements(DEPLOY_PROCESS_DEFINITION_TAG_NAME);
        List<String> fileNames = Lists.newArrayList();
        for (Element e : elements) {
            fileNames.add(e.attributeValue(FILE_ATTRIBUTE_NAME));
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }

    public static class Handler {

        public void onStartTransaction(byte[] script) {

        }

        public void onEndTransaction() {

        }

        public void onTransactionException(Exception e) {

        }

    }

}
