package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Link;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.Style;
import org.apache.ecs.html.TextArea;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.wfe.security.Permission;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "ChatTag")
public class ChatTag extends ProcessBaseFormTag {//ToDo replace extends class for chat

private static final long serialVersionUID = -4722799699002222875L;

private Long test;
@Attribute(required = false, rtexprvalue = true)
public void setTest1(Long test) {
this.test = test;
}

public Long getTest1() {
return test;
}

@Override
protected void fillFormData(TD tdFormElement) {
// TODO Auto-generated method stub
IMG imageOpenChat=new IMG();
imageOpenChat.setSrc("/wfe/images/chat_expand.png");
imageOpenChat.setID("imgButton");
imageOpenChat.setAlt("open");
imageOpenChat.setStyle("vertical-align: middle; width: 12px;  height: 12px;padding-bottom: 2px;");
IMG imageCloseChat=new IMG();
imageCloseChat.setSrc("/wfe/images/chat_roll_up.png");
imageCloseChat.setAlt("close");
imageCloseChat.setStyle("vertical-align: middle; width: 12px;  height: 12px;padding-bottom: 2px;");
Div  chatDiv=new Div();
chatDiv.setStyle("display: none");
chatDiv.setID("ChatForm");
//устанавливается id чата
chatDiv.addAttribute("chatId", getIdentifiableId());
TR updateVariableTR = new TR();
Button buttonOpenChat=new Button();

buttonOpenChat.setID("openChatButton");
buttonOpenChat.setType("button");
buttonOpenChat.setOnClick("test()");
buttonOpenChat.addElement("Открыть чат");


Button upScaleChat=new Button();
upScaleChat.addElement(imageOpenChat);
upScaleChat.setID("btnOp");
upScaleChat.setType("button");
//upScaleChat.setStyle("");

Button sendMessageChat=new Button();
sendMessageChat.addElement("Отправить");
sendMessageChat.setID("btnSend");
sendMessageChat.setType("button");
//sendMessageChat.setOnClick("send()");

Button closeChat=new Button();
closeChat.setID("close");
closeChat.addElement("&times");
closeChat.setType("button");

TextArea messageSend=new TextArea();
messageSend.setName("message");
messageSend.setID("message");
messageSend.setStyle("font-family: Verdana, Arial, Helvetica, sans-serif;\r\n" + 
		"            font-size: 12pt; /* Размер шрифта в пунктах */");

Input sendToAdress=new Input();
sendToAdress.setType("text");
sendToAdress.setID("sendToUser");
sendToAdress.setName("sendToUser");

Div myModal=new Div();
myModal.setClass("modal");
myModal.setID("myModal");

Div modalContetnt=new Div();
modalContetnt.setClass("modal-content");
modalContetnt.setStyle("width: 346px; position: fixed; top: auto; bottom: 0%; padding-top: 0px; margin-bottom: 0px; height: 506px; display: block; will-change: width, margin-right, right, transform, opacity, left, height; transform: translateY(0%); margin-right: 0px; margin-left: 30px; right: 60px;");

Div modalHeader=new Div();
Div modalHeaderDragg=new Div();
modalHeaderDragg.setClass("modal-header-dragg");
modalHeaderDragg.setID("modal-header-dragg");
modalHeaderDragg.setStyle("cursor: move;width: 220px;height:10px");
modalHeader.setClass("modal-header");
modalHeader.setID("modal-header");
//modalHeader.setStyle("cursor: move");

Div modalBody=new Div();
modalBody.setClass("modal-body");
modalBody.setStyle("height: 396px; width: 304px;");

Div modalFooter=new Div();
modalFooter.setClass("modal-footer");

Style styleButton=new Style();








styleButton.addElement("#myBtn{width: 100px;float:right;} #btnOp{background-color:#e9eaed ;} #btnCl{background-color:#e9eaed ;}  .modal {display: none;position: fixed; padding-top: 480px; left: 0;top: 0; padding-left:20%; width: 100vw;height: 100vh; overflow: auto;}.modal-content {position: relative;background-color: #e9eaed;margin: auto;padding: 0; border: 5px solid #e9eaed;width: 30vw;height: 40vh;box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2),0 6px 20px 0 rgba(0,0,0,0.19); -webkit-animation-name: animatetop;-webkit-animation-duration: 0.4s;animation-name: animatetop;animation-duration: 0.4s} .close {color: white; float: right;font-size: 28px;font-weight: bold;} .modal-header{padding: 2px 16px;background-color: #e9eaed;color: white; height: 30px;font-size: 11pt;} .modal-body {padding: 2px 16px;background-color: #e1e2e5;font-family: Verdana, Arial, Helvetica, sans-serif;font-size: 11pt;height: 390px;overflow: scroll;border: 1px solid #333;} .modal-footer {padding: 2px 16px; background-color: #e9eaed;color: white;} textarea { resize: none;}");
modalHeader.addElement(modalHeaderDragg);
modalHeader.addElement(closeChat);
modalHeader.addElement(upScaleChat);

modalContetnt.addElement(modalHeader);

modalContetnt.addElement(modalBody);

modalFooter.addElement(messageSend);
//modalFooter.addElement(sendToAdress);
modalFooter.addElement(sendMessageChat);
modalContetnt.addElement(modalFooter);
Script scrFromFile=new Script();
scrFromFile.setSrc("/wfe/js/chatPart1.js");
scrFromFile.setType("text/javascript");
Div box=new Div();
box.setClass("BoxContent");
updateVariableTR.addElement(styleButton);
updateVariableTR.addElement(buttonOpenChat);
chatDiv.addElement(modalContetnt);

box.addElement(updateVariableTR);
box.addElement(chatDiv);
tdFormElement.addElement(box);
}

@Override
protected Permission getSubmitPermission() {
// TODO Auto-generated method stub
return Permission.LIST;
}

}