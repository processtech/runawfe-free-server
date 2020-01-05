package ru.runa.wf.web.tag;

import org.apache.ecs.html.Button;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Style;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.apache.ecs.html.UL;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "ChatTag")
public class ChatTag extends SecuredObjectFormTag {

    private static final long serialVersionUID = -4722799699002222875L;
    private String type="default";
    private Long chatId = 0L;
    
    private String getType() {
        return type;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setType(String type) {
        this.type = type;
    }
    
	public Long getChatId() {
		return chatId;
	}

	@Attribute(required = false, rtexprvalue = true)
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

    @Override
    protected void fillFormData(TD tdFormElement) {
    	if(getType() == "TaskInProcess") {
    		WfTask task = Delegates.getTaskService().getTask(getUser(), getChatId());
    		setChatId(task.getProcessId());
    	}
        IMG imageChatExpand = new IMG();
        imageChatExpand.setSrc("/wfe/images/chat_roll_up.png");
        imageChatExpand.setAlt("close");
        imageChatExpand.setID("imgButton");
        Div chatDiv = new Div();
        chatDiv.setStyle("display: none");
        chatDiv.setID("ChatForm");
        // устанавливается id чата
        chatDiv.addAttribute("chatId", getChatId());
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
        modalBody.setID("modal-body");
        if (Delegates.getExecutorService().isAdministrator(getUser())) {
            modalBody.addAttribute("admin", "true");
        }

        Div modalFooter = new Div();
        modalFooter.setClass("modal-footer");
        modalFooter.setID("modalFooter");

        Style styleButton = new Style();

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

        Div boxButton = new Div();
        boxButton.setStyle("display:flex;padding-top: 5px;");
        boxButton.addElement(sendMessageChat);
        boxButton.addElement(fileInput);

        UL messageUserMention = new UL();
        messageUserMention.setClass("messageUserMention");

        modalFooter.addElement(messageUserMention);
        modalFooter.addElement(messageSend);
        modalFooter.addElement(boxButton);

        modalContetnt.addElement(modalFooter);
        Div dropZ = new Div();
        dropZ.setClass("dropZ");
        dropZ.setID("dropZ");
        //dropZ.addAttribute("hidden", "true");
        dropZ.addElement("Перетащите сюда файл");
        modalFooter.addElement(dropZ);

        Div attachedArea = new Div();
        attachedArea.setID("attachedArea");

        modalFooter.addElement(attachedArea);

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
/*
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
*/
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
        modalFooterSetting.setClass("modalFooterSetting");


        Button sendSetting = new Button();
        sendSetting.setType("button");
        sendSetting.setClass("acceptSettingsModal");
        sendSetting.setID("acceptSettingsModal");
        sendSetting.addElement("Принять");
        sendSetting.setStyle("float:right");
        modalFooterSetting.addElement(sendSetting);

        //modalSetting.addElement(modalHeaderSetting);
        modalSetting.addElement(modalBodySetting);
        modalSetting.addElement(modalFooterSetting);
        tdFormElement.addElement(modalSetting);
        tdFormElement.addElement(newMessagesIndicator);
        tdFormElement.addElement(box);
    }

    @Override
    protected Permission getSubmitPermission() {
        return null;
    }
    
	@Override
	protected SecuredObject getSecuredObject() {
		switch (getType()) {
		case "TaskInProcess":
			WfTask task = Delegates.getTaskService().getTask(getUser(), getChatId());
			return Delegates.getExecutionService().getProcess(getUser(), task.getProcessId());
		case "Process":
			return Delegates.getExecutionService().getProcess(getUser(), getChatId());
		default:
			return null;
		}
	}
	
}
