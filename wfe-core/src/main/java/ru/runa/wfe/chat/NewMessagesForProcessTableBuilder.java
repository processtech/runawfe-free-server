package ru.runa.wfe.chat;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

/**
 * Created on 16.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
public class NewMessagesForProcessTableBuilder {
    private final String baseUrl;
    private final int messageLimit;
    private final Process process;
    private final String processName;
    private final Map<ChatMessage, List<ChatMessageFile>> filesByMessages;
    private boolean isProcessLink;

    public NewMessagesForProcessTableBuilder(String baseUrl, int messageLimit, Process process, String processName, Map<ChatMessage, List<ChatMessageFile>> filesByMessages) {
        this.baseUrl = baseUrl;
        this.messageLimit = messageLimit;
        this.process = process;
        this.processName = processName;
        this.filesByMessages = filesByMessages;
    }

    public String build(boolean withProcessLink) {
        isProcessLink = withProcessLink;
        return createTableForProcess();
    }

    public String createTableForProcess() {
        return "<table style='margin-bottom: 10px; border-spacing: 0.5em 0.5em; width: 100%;'>" +
                "   <tbody>" +
                "       <tr>" +
                "           <td style='border-bottom: 1px solid #ccc;'>" +
                                createProcessField() +
                "           </td>" +
                "       </tr>" +
                        createMessageRows() +
                "   </tbody>" +
                "</table>\n";
    }

    private String createProcessField() {
        String text = "#" + process.getId() + ": " + processName;
        if (baseUrl != null && isProcessLink) {
            return "<a href='" + baseUrl + "/wfe/manage_process.do?id=" + process.getId() + "'>" + text + "</a>";
        }
        return text;
    }

    private String createMessageRows() {
        StringBuilder result = new StringBuilder();
        int messageCount = 0;
        for (Map.Entry<ChatMessage, List<ChatMessageFile>> entry : filesByMessages.entrySet()) {
            result.append("<tr><td style='padding: 0 10px;'>");
            result.append(createMessageTable(entry.getKey() , entry.getValue()));
            result.append("</td></tr>");
            if (++messageCount >= messageLimit) {
                break;
            }
        }
        if (filesByMessages.size() > messageLimit) {
            result.append(createAndMoreRow(filesByMessages.size() - messageLimit));
        }
        return result.toString();
    }

    private String createMessageTable(ChatMessage message, List<ChatMessageFile> files) {
        return "<table style='padding: 10px; width: 100%; padding: 0 10px; border-radius: 0 10px 10px 10px; background-color: rgb(0%, 0%, 0%, 0.04);'>\n" +
                "   <tbody>" +
                "       <tr>" +
                "           <td>" +
                                createActorField(message.getCreateActor()) +
                "               <div style='float: right;'>" +
                                    CalendarUtil.formatDateTime(message.getCreateDate()) +
                "               </div>" +
                "           </td>" +
                "       </tr>" +
                "       <tr>" +
                "           <td>" +
                                message.getText() +
                "           </td>" +
                "       </tr>" +
                        createFileFieldRow(files) +
                "   </tbody>" +
                "</table>";
    }

    private String createFileFieldRow(List<ChatMessageFile> files) {
        StringBuilder result = new StringBuilder();
        if (files.isEmpty()) {
            return "";
        }
        result.append("<tr><td style='padding: 0 10px;'>");
        result.append("<table><tbody>");
        for (ChatMessageFile file : files) {
            result.append("<tr><td>");
            result.append(createFileField(file));
            result.append("<td><tr>");
        }
        result.append("<tbody><table>");
        result.append("</td></tr>");
        return result.toString();
    }

    private String createFileField(ChatMessageFile file) {
        String text = "<span>\uD83D\uDCCE</span>" + file.getName();
        if (baseUrl != null) {
            return "<a href='" + baseUrl + "/wfe/chatFileOutput?fileId=" + file.getId() + "' download='" + file.getName() + "'>" + text + "</a>";
        }
        return text;
    }

    private String createActorField(Actor actor) {
        if (baseUrl != null) {
            return "<a href='" + baseUrl + "/wfe/manage_executor.do?id=" + actor.getId() + "'>" + actor.getName() + "</a>";
        }
        return actor.getName();
    }

    private String createAndMoreRow(int messageCount) {
        return "<tr><td style='padding: 0 20px;'><div>...</div>" +
                "<div>" + getAndMoreTextDependingNumber(messageCount) + "</div>" +
                "</td></tr>";
    }

    private String getAndMoreTextDependingNumber(int num) {
        int remainder = num % 100;
        if (remainder > 19) {
            remainder = remainder % 10;
        }
        String suffix;
        if (remainder == 1) {
            suffix = "новое сообщение";
        } else if (remainder >= 2 && remainder <= 4) {
            suffix = "новых сообщения";
        } else {
            suffix = "новых сообщений";
        }
        return "И еше " + num + " " + suffix;
    }
}
