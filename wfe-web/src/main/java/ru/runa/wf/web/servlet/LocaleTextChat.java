package ru.runa.wf.web.servlet;

import java.io.InputStream;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.user.User;

public class LocaleTextChat extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        String language = request.getParameter("language");
        InputStream input;
        Properties propLanguage = null;
        if (language.equals("ru")) {
            propLanguage = ClassLoaderUtil.getProperties("chat.localization.ru.properties", false);

        }
        String buttonSendMessage = propLanguage.getProperty("buttonSendMessage");
        String textAreaMessagePalceholder = propLanguage.getProperty("textAreaMessagePalceholder");
        String privateMessageCheckbox = propLanguage.getProperty("privateMessageCheckbox");
        String buttonLoadOldMessage = propLanguage.getProperty("buttonLoadOldMessage");
        String dropBlock = propLanguage.getProperty("dropBlock");
        String addReplyInMessageButton = propLanguage.getProperty("addReplyInMessageButton");
        String removeReplyInMessageButton = propLanguage.getProperty("removeReplyInMessageButton");
        String editMessageButton = propLanguage.getProperty("editMessageButton");
        String openChatButton = propLanguage.getProperty("openChatButton");
        // String switchChatButton = "";// ToDo
        // String newMessageIndicator = "";
        String attachedMessage = propLanguage.getProperty("attachedMessage");
        String warningRemoveMessage = propLanguage.getProperty("warningRemoveMessage");
        String warningEditMessage = propLanguage.getProperty("warningEditMessage");
        String openHierarchy = propLanguage.getProperty("openHierarchy");
        String closeHierarchy = propLanguage.getProperty("closeHierarchy");
        String quoteText = propLanguage.getProperty("quoteText");
        String errorMessFilePart1 = propLanguage.getProperty("errorMessFilePart1");
        String errorMessFilePart2 = propLanguage.getProperty("errorMessFilePart2");
        String textHeader = propLanguage.getProperty("textHeader");

        JSONObject outputObject = new JSONObject();
        outputObject.put("buttonSendMessage", buttonSendMessage);
        outputObject.put("textAreaMessagePalceholder", textAreaMessagePalceholder);
        outputObject.put("privateMessageCheckbox", privateMessageCheckbox);
        outputObject.put("buttonLoadOldMessage", buttonLoadOldMessage);
        outputObject.put("dropBlock", dropBlock);
        outputObject.put("addReplyInMessageButton", addReplyInMessageButton);
        outputObject.put("removeReplyInMessageButton", removeReplyInMessageButton);
        outputObject.put("editMessageButton", editMessageButton);
        outputObject.put("openChatButton", openChatButton);
        // outputObject.put("switchChatButton", switchChatButton);
        // outputObject.put("newMessageIndicator", newMessageIndicator);
        outputObject.put("attachedMessage", attachedMessage);
        outputObject.put("warningRemoveMessage", warningRemoveMessage);
        outputObject.put("warningEditMessage", warningEditMessage);
        outputObject.put("openHierarchy", openHierarchy);
        outputObject.put("closeHierarchy", closeHierarchy);
        outputObject.put("quoteText", quoteText);
        outputObject.put("closeHierarchy", errorMessFilePart2);
        outputObject.put("textHeader", textHeader);
        return outputObject;
    }
}