package ru.runa.wf.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.chat.ChatMessageFiles;
import ru.runa.wfe.service.delegate.Delegates;

public class ChatFileOutputServlet extends HttpServlet  {
    private static final long serialVersionUID = -5960818785099131021L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long fileId = Long.parseLong(request.getParameter("fileId"));
        ChatMessageFiles file = Delegates.getExecutionService().getChatMessageFile(fileId);
        response.getOutputStream().write(file.getFile());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long fileId = Long.parseLong(request.getParameter("fileId"));
        ChatMessageFiles file = Delegates.getExecutionService().getChatMessageFile(fileId);
        response.getOutputStream().write(file.getFile());
    }
}
