package ru.runa.wfe.chat.sender;

import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatMessageDao;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Created on 07.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
public class ChatEmailNotificationJob {
    private static final Log log = LogFactory.getLog(ChatEmailNotificationJob.class);

    @Autowired
    private ChatMessageDao chatMessageDao;
    @Autowired
    private ChatFileDao chatFileDao;
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private ProcessDao processDao;

    private byte[] configBytes;
    private String baseUrl;
    private final Map<String, Set<Long>> sentMessagesForEmails = new HashMap<>();
    private static final int messageLimit = 3;

    @Required
    public void setConfigLocation(String path) {
        try {
            InputStream configInputStream = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
            configBytes = ByteStreams.toByteArray(configInputStream);
        } catch (Exception e) {
            log.error("Configuration error: " + e);
        }
    }

    @Required
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void execute() {
        if (configBytes == null) {
            return;
        }
        List<Actor> actors;
        int page = 0;
        while (!(actors = executorDao.getAllActorsWithPagination(page++, 10)).isEmpty()) {
            for (Actor actor : actors) {
                List<String> emailsToSend = EmailUtils.getEmails(actor);
                String emails = EmailUtils.concatenateEmails(emailsToSend);
                List<ChatMessage> messages = chatMessageDao.getNewMessagesByActor(actor);
                if (messages.isEmpty() || isEmailAlreadySent(emails, messages)) {
                    continue;
                }
                EmailConfig config = EmailConfigParser.parse(configBytes);
                config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
                config.clearMessage();
                int messageCount = messages.size();
                String suffix;
                if (messageCount == 1) {
                    suffix = "непрочитанное сообщение";
                } else if (messageCount >= 2 && messageCount <= 4) {
                    suffix = "непрочитанных сообщения";
                } else {
                    suffix = "непрочитанных сообщений";
                }
                config.getHeaderProperties().put("Subject", "У Вас " + messageCount + " " + suffix);
                Map<Process, List<ChatMessage>> processNewMessagesMap = mappingByProcess(messages);
                try {
                    for (Map.Entry<Process, List<ChatMessage>> entry : processNewMessagesMap.entrySet()) {
                        if (entry.getValue().isEmpty()) {
                            continue;
                        }
                        String processName = processDao.getDefinitionName(entry.getKey());
                        config.setMessage(config.getMessage() + createProcessTable(actor, entry.getKey(), processName, entry.getValue()));
                    }
                    if (config.getMessage().isEmpty()) {
                        continue;
                    }
                    EmailUtils.sendMessage(config);
                    setSentMessagesForEmails(messages, emails);
                } catch (Exception e) {
                    log.error("Email notification to: " + emails + " send error: " + e);
                }
            }
        }
    }

    private boolean isEmailAlreadySent(String emails, List<ChatMessage> newMessages) {
        Set<Long> messagesId = new HashSet<>();
        for (ChatMessage message : newMessages) {
            messagesId.add(message.getId());
        }
        return Objects.equals(messagesId, sentMessagesForEmails.get(emails));
    }

    private void setSentMessagesForEmails(List<ChatMessage> messages, String emails) {
        Set<Long> sentMessages = new HashSet<>();
        for (ChatMessage message : messages) {
            sentMessages.add(message.getId());
        }
        sentMessagesForEmails.put(emails, sentMessages);
    }

    private Map<Process, List<ChatMessage>> mappingByProcess(List<ChatMessage> messages) {
        Map<Process, List<ChatMessage>> result = new HashMap<>();
        for (ChatMessage message : messages) {
            Process process = message.getProcess();
            if (result.containsKey(process)) {
                result.get(process).add(message);
            } else {
                List<ChatMessage> messagesList = new ArrayList<>();
                messagesList.add(message);
                result.put(process, messagesList);
            }
        }
        return result;
    }

    private String createProcessTable(Actor actor, Process process, String processName, List<ChatMessage> messages) {
        return String.join("\n",
                "<table style='margin-bottom: 10px; border-spacing: 0.5em 0.5em; width: 100%;'>",
                "   <tbody>",
                "       <tr>",
                "           <td style='border-bottom: 1px solid #ccc;'>",
                                createProcessField(actor, process, processName),
                "           </td>",
                "       </tr>",
                        createMessageRows(messages),
                "   </tbody>",
                "</table>"
        );
    }

    public String createProcessField(Actor actor, Process process, String processName) {
        StringBuilder result = new StringBuilder();
        String text = "#" + process.getId() + ": " + processName;
        if (baseUrl != null && permissionDao.isAllowed(actor, Permission.READ, process.getSecuredObjectType(), process.getIdentifiableId())) {
            result.append("<a href='").append(baseUrl).append("/wfe/manage_process.do?id=").append(process.getId()).append("'>");
            result.append(text);
            result.append("</a>");
        } else {
            result.append(text);
        }
        return result.toString();
    }

    private String createMessageRows(List<ChatMessage> messages) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < messages.size() && i < messageLimit; i++) {
            result.append("<tr><td style='padding: 0 10px;'>");
            result.append(createMessageTable(messages.get(i)));
            result.append("</td></tr>");
        }
        if (messages.size() > messageLimit) {
            result.append(createAndMoreRow(messages.size() - messageLimit));
        }
        return result.toString();
    }

    private String createMessageTable(ChatMessage message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return String.join("\n",
                "<table style='padding: 10px; width: 100%; padding: 0 10px; border-radius: 0 10px 10px 10px; background-color: rgb(0%, 0%, 0%, 0.04);'>",
                "   <tbody>",
                "       <tr>",
                "           <td>",
                                createActorField(message.getCreateActor()),
                "               <div style='float: right;'>",
                                    dateFormat.format(message.getCreateDate()),
                "               </div>",
                "           </td>",
                "       </tr>",
                "       <tr>",
                "           <td>",
                                message.getText(),
                "           </td>",
                "       </tr>",
                        createFileFieldRow(message),
                "   </tbody>",
                "</table>"
        );
    }

    private String createFileFieldRow(ChatMessage message) {
        StringBuilder result = new StringBuilder();
        List<ChatMessageFile> files = chatFileDao.getByMessage(message);
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
        } else {
            return text;
        }
    }

    private String createActorField(Actor actor) {
        StringBuilder result = new StringBuilder();
        if (baseUrl != null) {
            result.append("<a href='").append(baseUrl).append("/wfe/manage_executor.do?id=").append(actor.getId()).append("'>");
            result.append(actor.getName());
            result.append("</a>");
        } else {
            result.append(actor.getName());
        }
        return result.toString();
    }

    public String createAndMoreRow(int messageCount) {
        String suffix;
        if (messageCount == 1) {
            suffix = "новое сообщение";
        } else if (messageCount >= 2 && messageCount <= 4) {
            suffix = "новых сообщения";
        } else {
            suffix = "новых сообщений";
        }
        return String.join("\n",
                "<tr><td style='padding: 0 20px;'>",
                "   <div>...</div>",
                "   <div>И еще " + messageCount + " " + suffix + "</div>",
                "</td></tr>"
        );
    }
}
