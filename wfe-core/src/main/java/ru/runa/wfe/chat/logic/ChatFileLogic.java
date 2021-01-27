package ru.runa.wfe.chat.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import ru.runa.wfe.chat.ChatFileLoadException;
import ru.runa.wfe.chat.ChatFileSaveException;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dao.ChatDao;
import ru.runa.wfe.chat.dto.ChatMessageDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * @author Sergey Inyakin
 */
@PropertySource("classpath:system.properties")
public class ChatFileLogic extends WfCommonLogic {

    @Autowired
    private ChatDao chatDao;
    @Autowired
    private ChatLogic chatLogic;
    @Value("${chat.files.storage.path}")
    private String storagePath;
    @Autowired
    private ChatMessageFileMapper messageFileMapper;

    public void deleteFile(User user, Long id) {
        chatDao.deleteFile(user, id);
    }

    public ChatMessageDto saveMessageAndBindFiles(Long processId, ChatMessage message, Set<Executor> mentionedExecutors,
            Boolean isPrivate,
            ArrayList<ChatMessageFileDto> files) {
        message.setProcess(processDao.get(processId));
        Set<Executor> executors;
        if (!isPrivate) {
            executors = chatLogic.getAllUsers(message.getProcess().getId(), message.getCreateActor());
        } else {
            executors = new HashSet<>(mentionedExecutors);
        }
        List<ChatMessageFile> messageFiles = writeAllFilesToFS(files);
        return chatDao.saveMessageAndBindFiles(message, messageFiles, executors, mentionedExecutors);
    }

    public List<ChatMessageFileDto> getMessageFiles(Actor actor, ChatMessage message) {
        List<ChatMessageFileDto> result = new ArrayList<>();
        for (ChatMessageFile messageFile : chatDao.getMessageFiles(actor, message)) {
            result.add(readFileWithFS(messageFile));
        }
        return result;
    }

    public ChatMessageFileDto saveMessageFile(ChatMessageFileDto dto) {
        return readFileWithFS(chatDao.saveFile(writeFileToFS(dto)));
    }

    public ChatMessageFileDto getMessageFile(Actor actor, Long fileId) {
        return readFileWithFS(chatDao.getFile(actor, fileId));
    }

    private List<ChatMessageFile> writeAllFilesToFS(ArrayList<ChatMessageFileDto> dtos) {
        List<ChatMessageFile> result = new ArrayList<>();
        try {
            for (ChatMessageFileDto dto : dtos)
                result.add(writeFileToFS(dto));
        } catch (Exception exception) {
            for (ChatMessageFile messageFile : result)
                deleteFileWithFS(Paths.get(storagePath + "/" + messageFile.getUuid()));
            throw exception;
        }
        return result;
    }

    private ChatMessageFile writeFileToFS(ChatMessageFileDto dto) {
        ChatMessageFile result = messageFileMapper.toEntity(dto);
        String uuidName;
        Path path;
        do {
            uuidName = UUID.randomUUID().toString();
            path = Paths.get(storagePath + "/" + uuidName);
        } while (Files.isRegularFile(path));
        try {
            Files.write(path, dto.getBytes());
            result.setUuid(uuidName);
        } catch (Exception e) {
            deleteFileWithFS(path);
            throw new ChatFileSaveException(result);
        }
        return result;
    }

    private ChatMessageFileDto readFileWithFS(ChatMessageFile messageFile) {
        ChatMessageFileDto result;
        try {
            result = messageFileMapper.toDto(messageFile);
            byte[] bytes = Files.readAllBytes(Paths.get(storagePath + "/" + messageFile.getUuid()));
            result.setBytes(bytes);
        } catch (IOException e) {
            throw new ChatFileLoadException(messageFile);
        }
        return result;
    }

    private void deleteFileWithFS(Path path) {
        try {
            Files.delete(path);
        } catch (IOException exception) {
            log.error(exception);
        }
    }
}
