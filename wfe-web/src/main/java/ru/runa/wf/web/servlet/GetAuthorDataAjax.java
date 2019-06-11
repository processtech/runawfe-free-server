package ru.runa.wf.web.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.logic.ChatLogic;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.user.User;

public class GetAuthorDataAjax extends JsonAjaxCommand {
	
	

	@Override
	protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
		JSONObject object = new JSONObject();
	    object.put("author", user.getName().toString());
        return object;
	   
	}
	
}
