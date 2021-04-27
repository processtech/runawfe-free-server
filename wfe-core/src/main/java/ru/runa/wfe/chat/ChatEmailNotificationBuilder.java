package ru.runa.wfe.chat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Actor;

/**
 * Created on 16.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
public class ChatEmailNotificationBuilder {
    private static final int MESSAGE_LIMIT = 3;
    private String baseUrl;

    private int newMessagesCount;
    private Actor actor;
    private Map<Process, List<ChatMessage>> messages = new HashMap<>();
    private Map<Process, String> processesNames = new HashMap<>();
    private Map<Process, Boolean> permissions = new HashMap<>();
    private Map<ChatMessage, List<ChatMessageFile>> files = new HashMap<>();

    public ChatEmailNotificationBuilder setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ChatEmailNotificationBuilder setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
        return this;
    }

    public ChatEmailNotificationBuilder setActor(Actor actor) {
        this.actor = actor;
        return this;
    }

    public ChatEmailNotificationBuilder setMessages(Map<Process, List<ChatMessage>> messages) {
        this.messages = messages;
        return this;
    }

    public ChatEmailNotificationBuilder setProcessesNames(Map<Process, String> processesNames) {
        this.processesNames = processesNames;
        return this;
    }

    public ChatEmailNotificationBuilder setPermissions(Map<Process, Boolean> permissions) {
        this.permissions = permissions;
        return this;
    }

    public ChatEmailNotificationBuilder setFiles(Map<ChatMessage, List<ChatMessageFile>> files) {
        this.files = files;
        return this;
    }

    public EmailConfig build(byte[] configBytes) {
        List<String> emailsToSend = EmailUtils.getEmails(actor);
        String emails = EmailUtils.concatenateEmails(emailsToSend);
        EmailConfig config = EmailConfigParser.parse(configBytes);
        config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
        config.getHeaderProperties().put("Subject", getSubjectDependingNumber(newMessagesCount));
        StringBuilder result = new StringBuilder();
        for (Process process : messages.keySet()) {
            result.append(createTableForProcess(process));
        }
        config.setMessage(result.toString());
        return config;
    }

    private String createTableForProcess(Process process) {
        return "<table style='margin-bottom: 10px; border-spacing: 0.5em 0.5em; width: 100%;'>" +
                "   <tbody>" +
                "       <tr>" +
                "           <td style='border-bottom: 1px solid #ccc;'>" +
                                createProcessField(process) +
                "           </td>" +
                "       </tr>" +
                        createMessageRows(process) +
                "   </tbody>" +
                "</table>\n";
    }

    private String createProcessField(Process process) {
        String text = "#" + process.getId() + ": " + processesNames.get(process);
        if (baseUrl != null && permissions.get(process)) {
            return "<a href='" + baseUrl + "/wfe/manage_process.do?id=" + process.getId() + "'>" + text + "</a>";
        }
        return text;
    }

    private String createMessageRows(Process process) {
        StringBuilder result = new StringBuilder();
        Map<ChatMessage, List<ChatMessageFile>> messagesByProcess = getMessagesByProcess(process);
        int messageCount = 0;
        for (Map.Entry<ChatMessage, List<ChatMessageFile>> entry : messagesByProcess.entrySet()) {
            result.append("<tr><td style='padding: 0 10px;'>");
            result.append(createMessageTable(entry.getKey(), entry.getValue()));
            result.append("</td></tr>");
            if (++messageCount >= MESSAGE_LIMIT) {
                break;
            }
        }
        if (messagesByProcess.size() > MESSAGE_LIMIT) {
            result.append(createAndMoreRow(messagesByProcess.size() - MESSAGE_LIMIT));
        }
        return result.toString();
    }

    public Map<ChatMessage, List<ChatMessageFile>> getMessagesByProcess(Process process) {
        Map<ChatMessage, List<ChatMessageFile>> result = new LinkedHashMap<>();
        for (ChatMessage message : messages.get(process)) {
            result.put(message, files.get(message));
        }
        return result;
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
        if (files.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
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
        String suffix = getTextDependingNumber(num,
                new String[] {"новое сообщение", "новых сообщения", "новых сообщений"});
        return "И ещё " + num + " " + suffix;
    }

    private String getSubjectDependingNumber(int num) {
        String suffix = getTextDependingNumber(num,
                new String[] {"непрочитанное сообщение", "непрочитанных сообщения", "непрочитанных сообщений"});
        return "У вас " + num + " " + suffix;
    }

    String getTextDependingNumber(int num, String[] texts) {
        int remainder = num % 100;
        if (remainder > 19) {
            remainder = remainder % 10;
        }
        if (remainder == 1) {
            return texts[0];
        } else if (remainder >= 2 && remainder <= 4) {
            return texts[1];
        } else {
            return texts[2];
        }
    }
}
