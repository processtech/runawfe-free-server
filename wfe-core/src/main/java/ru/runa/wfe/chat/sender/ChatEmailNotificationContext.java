package ru.runa.wfe.chat.sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.execution.Process;

/**
 * Created on 23.04.2021
 *
 * @author Sergey Inyakin
 * @since 2148
 */
public class ChatEmailNotificationContext {
    private int messagesSize;
    private final Map<Process, List<ChatMessage>> messages = new HashMap<>();
    private Map<Process, String> processesNames = new HashMap<>();
    private Map<Process, Boolean> permissions = new HashMap<>();
    private Map<ChatMessage, List<ChatMessageFile>> files = new HashMap<>();

    public void setMessages(List<ChatMessage> messages) {
        messagesSize = messages.size();
        for (ChatMessage message : messages) {
            this.messages.computeIfAbsent(message.getProcess(), new ComputeIfAbsentFunction()).add(message);
        }
    }

    public Map<Process, List<ChatMessage>> getMessages() {
        return messages;
    }

    public int getMessagesSize() {
        return messagesSize;
    }

    public Set<Process> getProcesses() {
        return messages.keySet();
    }

    public void setProcessesNames(Map<Process, String> processesNames) {
        this.processesNames = processesNames;
    }

    public String getNameByProcess(Process process) {
        return processesNames.get(process);
    }

    public void setPermissions(Map<Process, Boolean> permissions) {
        this.permissions = permissions;
    }

    public boolean isPermissionReadByProcess(Process process) {
        return permissions.getOrDefault(process, false);
    }

    public void setFiles(Map<ChatMessage, List<ChatMessageFile>> files) {
        this.files = files;
    }

    public Map<ChatMessage, List<ChatMessageFile>> getMessagesByProcess(Process process) {
        Map<ChatMessage, List<ChatMessageFile>> result = new LinkedHashMap<>();
        for (ChatMessage message : messages.get(process)) {
            result.put(message, files.get(message));
        }
        return result;
    }

    private static class ComputeIfAbsentFunction implements Function<Process, List<ChatMessage>> {
        @Override
        public List<ChatMessage> apply(Process process) {
            return new ArrayList<>();
        }
    }
}
