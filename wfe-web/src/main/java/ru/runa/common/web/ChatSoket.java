package ru.runa.common.web;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONObject;

@ApplicationScoped
@ServerEndpoint("/actions")
public class ChatSoket {

@Inject
private ChatSessionHandler sessionHandler;
	
 @OnOpen
 public void open(Session session) {
	 sessionHandler.addSession(session);
	 JSONObject firstMess= new JSONObject();
	 firstMess.put("text", "Start soket");
	 sessionHandler.sendToSession(session,firstMess);
 }

 @OnClose
 public void close(Session session) {
	 sessionHandler.removeSession(session);
 }

 @OnError
 public void onError(Throwable error) {
 }

 @OnMessage
 public void handleMessage(String message, Session session) {
	 JSONObject firstMess= new JSONObject();
	 firstMess.put("text", "Start soket");
	 sessionHandler.sendToSession(session,firstMess);
 }
 
}
