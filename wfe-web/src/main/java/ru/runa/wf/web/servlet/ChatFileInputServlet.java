package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import ru.runa.wfe.chat.ChatMessageFiles;
import ru.runa.wfe.service.delegate.Delegates;

@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1024, // 1 гб
        maxFileSize = 1024 * 1024 * 1024, // 1 гб
        maxRequestSize = 1024 * 1024 * 1024) // 1 гб
public class ChatFileInputServlet extends HttpServlet {

    private static final long serialVersionUID = 5028849216708724335L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long mesId = Long.parseLong(request.getParameter("messageId"));
        String fileName = request.getParameter("fileName");
        String endFlag = request.getParameter("endFlag");
        Part file = request.getPart("file");
        ChatMessageFiles chatFile = new ChatMessageFiles();
        chatFile.setFileName(fileName);
        chatFile.setMessageId(Delegates.getExecutionService().getChatMessage(mesId));

        byte[] FileMass = new byte[(int) file.getSize()];
        InputStreamReader reader = new InputStreamReader(file.getInputStream());
        file.getInputStream().read(FileMass);
        chatFile.setFile(FileMass);
        Delegates.getExecutionService().saveChatMessageFile(chatFile);
    }

}
