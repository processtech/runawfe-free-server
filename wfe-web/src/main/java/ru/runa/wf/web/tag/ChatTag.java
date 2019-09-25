package ru.runa.wf.web.tag;

import org.apache.ecs.html.Button;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.Style;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.tldgen.annotations.BodyContent;

import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "ChatTag")
public class ChatTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = -4722799699002222875L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        IMG imageChatExpand = new IMG();
        imageChatExpand.setSrc("/wfe/images/chat_roll_up.png");
        imageChatExpand.setAlt("close");
        imageChatExpand.setID("imgButton");
        imageChatExpand.setStyle("vertical-align: middle; width: 12px;  height: 12px;padding-bottom: 2px;");
        Div chatDiv = new Div();
        chatDiv.setStyle("display: none");
        chatDiv.setID("ChatForm");
        // устанавливается id чата
        chatDiv.addAttribute("chatId", getIdentifiableId());
        TR updateVariableTR = new TR();
        Button buttonOpenChat = new Button();

        buttonOpenChat.setID("openChatButton");
        buttonOpenChat.setType("button");
        buttonOpenChat.setOnClick("test()");
        buttonOpenChat.addElement("Открыть чат");

        Button upScaleChat = new Button();
        upScaleChat.addElement(imageChatExpand);
        upScaleChat.setID("btnOp");
        upScaleChat.setType("button");

        Button sendMessageChat = new Button();
        sendMessageChat.addElement("Отправить");
        sendMessageChat.setID("btnSend");
        sendMessageChat.setType("button");

        Button closeChat = new Button();
        closeChat.setID("close");
        closeChat.addElement("&times");
        closeChat.setType("button");

        TextArea messageSend = new TextArea();
        messageSend.setName("message");
        messageSend.setID("message");
        messageSend.addAttribute("placeholder", "Введите текст сообщения");

        Input sendToAdress = new Input();
        sendToAdress.setType("text");
        sendToAdress.setID("sendToUser");
        sendToAdress.setName("sendToUser");

        Div myModal = new Div();
        myModal.setClass("modal");
        myModal.setID("myModal");

        Div modalContetnt = new Div();
        modalContetnt.setClass("modal-content");
        modalContetnt.setStyle(
                "width: 346px; position: fixed; top: auto; bottom: 0%; padding-top: 0px; margin-bottom: 0px;height: 585px; display: block; will-change: width, margin-right, right, transform, opacity, left, height; transform: translateY(0%); margin-right: 0px; margin-left: 30px; right: 60px;");

        Div modalHeader = new Div();
        Div modalHeaderDragg = new Div();
        modalHeaderDragg.setClass("modal-header-dragg");
        modalHeaderDragg.setID("modal-header-dragg");

        modalHeader.setClass("modal-header");
        modalHeader.setID("modal-header");
        // modalHeader.setStyle("cursor: move");
        Button loadNewMessage = new Button();
        loadNewMessage.setID("loadNewBessageButton");
        loadNewMessage.addElement("Загрузить сообщения выше");
        loadNewMessage.setType("button");

        Div modalBody = new Div();
        modalBody.setClass("modal-body");
        if (Delegates.getExecutorService().isAdministrator(getUser()))
         {
            modalBody.addAttribute("admin", "true");
        }
        modalBody.setStyle("height: 396px; width: 304px;");

        Div modalFooter = new Div();
        modalFooter.setClass("modal-footer");
        modalFooter.setID("modalFooter");





        Style styleButton = new Style();

        styleButton.addElement(
                "#myBtn{width: 100px;float:right;} #btnOp{background-color:#e9eaed ;} #btnCl{background-color:#e9eaed ;}  .modal {display: none;position: fixed; padding-top: 480px; left: 0;top: 0; padding-left:20%; width: 100vw;height: 100vh; overflow: auto;}.modal-content {position: relative;background-color: #e9eaed;margin: auto;padding: 0; border: 5px solid #e9eaed;width: 30vw;height: 40vh;box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19); -webkit-animation-name: animatetop;-webkit-animation-duration: 0.4s;animation-name: animatetop;animation-duration: 0.4s} .close {color: white; float: right;font-size: 28px;font-weight: bold;} .modal-header{padding: 2px 16px;background-color: #e9eaed;color: white; height: 30px;font-size: 11pt;} .modal-body {padding: 2px 16px;background-color: #e1e2e5;font-family: Verdana, Arial, Helvetica, sans-serif;font-size: 11pt;height: 390px;overflow: scroll;border: 1px solid #333;} .modal-footer {padding: 2px 16px; background-color: #e9eaed;color: white;} ");
        modalHeader.addElement(modalHeaderDragg);
        modalHeader.addElement(closeChat);
        modalHeader.addElement(upScaleChat);
        Button modalSettings = new Button();
        modalSettings.setClass("modalSettings");
        modalSettings.setStyle("float:right");
        modalSettings.addElement("Настройки");
        modalSettings.setType("button");

        modalHeader.addElement(modalSettings);

        modalContetnt.addElement(modalHeader);
        modalBody.addElement(loadNewMessage);
        modalContetnt.addElement(modalBody);

        Input fileInput = new Input();
        fileInput.setType("file");
        fileInput.setSize(0);
        fileInput.setID("fileInput");
        fileInput.addAttribute("multiple", "true");

        Div messageUserMention = new Div();
        messageUserMention.setClass("messageUserMention");

        modalFooter.addElement(messageUserMention);
        modalFooter.addElement(messageSend);
        modalFooter.addElement(sendMessageChat);
        modalFooter.addElement(fileInput);

        modalContetnt.addElement(modalFooter);
        Div dropZ = new Div();
        dropZ.setClass("dropZ");
        dropZ.setID("dropZ");
        dropZ.addAttribute("hidden", "true");
        dropZ.addElement("Перетащите сюда файл");
        modalContetnt.addElement(dropZ);

        Div attachedArea = new Div();
        attachedArea.setID("attachedArea");

        modalContetnt.addElement(attachedArea);

        Div box = new Div();
        box.setClass("BoxContent");
        updateVariableTR.addElement(styleButton);
        updateVariableTR.addElement(buttonOpenChat);
        chatDiv.addElement(modalContetnt);

        box.addElement(updateVariableTR);
        box.addElement(chatDiv);
        Div newMessagesIndicator = new Div();
        Div countNewMessages = new Div();
        countNewMessages.setID("countNewMessages");
        countNewMessages.setClass("countNewMessages");
        newMessagesIndicator.addElement("У вас ");
        newMessagesIndicator.addElement(countNewMessages);
        newMessagesIndicator.addElement(" пропущеных сообщений");
        newMessagesIndicator.setID("newMessagesIndicator");
        newMessagesIndicator.setClass("newMessagesIndicator");

        Div modalSetting = new Div();
        modalSetting.setClass("modalSetting");

        Div modalHeaderSetting = new Div();
        modalHeaderSetting.setClass("modalHeaderSetting");

        Button closeButtonModalSetting = new Button();
        closeButtonModalSetting.setType("button");
        closeButtonModalSetting.setClass("closeButtonModalSetting");
        closeButtonModalSetting.addElement("X");
        closeButtonModalSetting.setStyle("float:right;");
        modalHeaderSetting.addElement(closeButtonModalSetting);
        P headName = new P();
        headName.addElement("Настройки");
        headName.setStyle("float:left;");
        modalHeaderSetting.addElement(closeButtonModalSetting);
        modalHeaderSetting.addElement(headName);

        Div modalBodySetting = new Div();
        modalBodySetting.setClass("modalBodySetting");
        Table tableSettingWin = new Table();
        tableSettingWin.setID("userTableSetting");// таблица настроек
        TR trowMain = new TR();
        trowMain.setClass("throwMain");
        TD tname = new TD();
        tname.addElement("Имя");
        TD tcheck = new TD();
        tcheck.addElement("Выделение");

        trowMain.addElement(tcheck);
        trowMain.addElement(tname);
        TR contentThrow = new TR();
        contentThrow.setClass("contentThrow");

        tableSettingWin.addElement(trowMain);

        tableSettingWin.addElement(contentThrow);

        modalBodySetting.addElement(tableSettingWin);

        Div modalFooterSetting = new Div();
        modalFooterSetting.setClass("modalFooterSetting");;

        Button sendSetting = new Button();
        sendSetting.setType("button");
        sendSetting.setClass("acceptSettingsModal");
        sendSetting.setID("acceptSettingsModal");
        sendSetting.addElement("Принять");
        sendSetting.setStyle("float:right");
        modalFooterSetting.addElement(sendSetting);

        modalSetting.addElement(modalHeaderSetting);
        modalSetting.addElement(modalBodySetting);
        modalSetting.addElement(modalFooterSetting);
        tdFormElement.addElement(modalSetting);
        tdFormElement.addElement(newMessagesIndicator);
        tdFormElement.addElement(box);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

}