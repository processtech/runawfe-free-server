package ru.runa.wf.web.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ChatFileOutputServlet extends HttpServlet {
    private static final long serialVersionUID = -5960818785099131021L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = Commons.getUser(request.getSession());
        Long fileId = Long.parseLong(request.getParameter("fileId"));
        ChatMessageFileDto file = Boolean.parseBoolean(request.getParameter("archived"))
                ? Delegates.getChatService().getArchiveChatMessageFile(user, fileId)
                : Delegates.getChatService().getChatMessageFile(user, fileId);
        String encodedFileName = HTMLUtils.encodeFileName(request, file.getName());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.getOutputStream().write(file.getBytes());
    }
}
