package ru.runa.wf.web.servlet;

import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.user.User;

public class LocaleTextChat extends JsonAjaxCommand {
    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {

        String buttonSendMessage = "";
        String textAreaMessagePalceholder = "";
        String privateMessageCheckbox = "";
        String buttonLoadOldMessage = "";
        String dropBlock = "";
        String addReplyInMessageButton = "";
        String removeReplyInMessageButton = "";
        String editMessageButton = "";
        String openChatButton = "";
        String switchChatButton = "";
        String newMessageIndicator = "";
        String attachedMessage = "";
        String warningRemoveMessage = "Вы действительно хотите удалить сообщение? Отменить это действие будет невозможно";
        String warningEditMessage = "Вы действительно хотите отредактировать сообщение? Отменить это действие будет невозможно";
        String openHierarchy = "";
        String closeHierarchy = "";
        String quoteText = "Цитата";
        String errorMessFilePart1 = "Ошибка. Размер файла превышен на ";
        String errorMessFilePart2 = " байт, максимальный размер файла = ";

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
        outputObject.put("switchChatButton", switchChatButton);
        outputObject.put("newMessageIndicator", newMessageIndicator);
        outputObject.put("attachedMessage", attachedMessage);
        outputObject.put("warningRemoveMessage", warningRemoveMessage);
        outputObject.put("warningEditMessage", warningEditMessage);
        outputObject.put("openHierarchy", openHierarchy);
        outputObject.put("closeHierarchy", closeHierarchy);
        outputObject.put("quoteText", quoteText);
        outputObject.put("closeHierarchy", errorMessFilePart2);
        return outputObject;
    }
}