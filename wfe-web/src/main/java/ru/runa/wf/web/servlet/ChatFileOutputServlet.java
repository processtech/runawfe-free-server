package ru.runa.wf.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.runa.common.web.Commons;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ChatFileOutputServlet extends HttpServlet {
    private static final long serialVersionUID = -5960818785099131021L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = Commons.getUser(request.getSession());
        Long fileId = Long.parseLong(request.getParameter("fileId"));
        ChatMessageFile file = Delegates.getChatService().getChatMessageFile(user, fileId);
        response.getOutputStream().write(file.getBytes());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = Commons.getUser(request.getSession());
        Long fileId = Long.parseLong(request.getParameter("fileId"));
        ChatMessageFile file = Delegates.getChatService().getChatMessageFile(user, fileId);
        response.getOutputStream().write(file.getBytes());
    }
}
