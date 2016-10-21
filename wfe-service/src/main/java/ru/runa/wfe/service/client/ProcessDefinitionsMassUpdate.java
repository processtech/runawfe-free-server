package ru.runa.wfe.service.client;

import java.io.File;
import java.io.FilenameFilter;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.utils.AdminScriptUtils;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public class ProcessDefinitionsMassUpdate {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: ProcessDefinitionsMassUpdate <dirpath> <username> <password>");
            System.out.println("Example: ProcessDefinitionsMassUpdate . foo $eCreTw0rd");
            System.exit(-1);
        }
        File directory = new File(args[0]);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Directory " + args[0] + " is invalid");
            System.exit(-1);
        }
        File[] parFiles = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("par");
            }
        });
        try {
            Document document = AdminScriptUtils.createScriptDocument();
            for (File parFile : parFiles) {
                Element element = document.getRootElement().addElement("redeployProcessDefinition", XmlUtils.RUNA_NAMESPACE);
                String fileName = parFile.getName();
                String definitionName = fileName.substring(0, fileName.length() - 4);
                element.addAttribute("name", definitionName);
                element.addAttribute("file", parFile.getAbsolutePath());
            }
            String script = XmlUtils.toString(document);
            System.out.println("Sending script \n");
            System.out.println(script);
            byte[] scriptBytes = script.getBytes(Charsets.UTF_8);
            User user = Delegates.getAuthenticationService().authenticateByLoginPassword(args[1], args[2]);
            Delegates.getScriptingService().executeAdminScript(user, scriptBytes, null);
            System.out.println("Finished");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
