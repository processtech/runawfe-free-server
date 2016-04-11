package ru.runa.alfresco;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Web services connection settings.
 * 
 * @author dofs
 */
public class WSConnectionSettings extends Settings {
    private final String endpointAddress;
    private final String login;
    private final String password;

    private WSConnectionSettings(String endpointAddress, String login, String password) {
        this.endpointAddress = endpointAddress;
        this.login = login;
        this.password = password;
    }

    private static WSConnectionSettings instance;

    public static WSConnectionSettings getInstance() {
        if (instance == null) {
            synchronized (WSConnectionSettings.class) {
                if (instance == null) {
                    try {
                        Document document = getConfigDocument();
                        Element root = document.getRootElement();
                        Element connectionElement = root.element("connection");
                        String systemLogin = connectionElement.attributeValue("login");
                        String systemPassword = connectionElement.attributeValue("password");
                        String endpointAddress = connectionElement.attributeValue("endpoint");
                        instance = new WSConnectionSettings(endpointAddress, systemLogin, systemPassword);
                    } catch (Throwable e) {
                        log.error("Unable to load ws connection info", e);
                    }
                }
            }
        }
        return instance;
    }

    public static WSConnectionSettings getInstance(String hostname, int port, String login, String password) {
        if (instance == null) {
            synchronized (WSConnectionSettings.class) {
                if (instance == null) {
                    instance = new WSConnectionSettings("http://" + hostname + ":" + port + "/alfresco/api", login, password);
                }
            }
        }
        return instance;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getAlfBaseUrl() {
        // 'api' removal at the end
        return endpointAddress.substring(0, endpointAddress.length() - 3);
    }

}
