package ru.runa.wfe.chat.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatFileIoException;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.commons.SystemProperties;

/**
 * @author Sergey Inyakin
 */
@CommonsLog
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChatFileIo {
    private final String storagePath = SystemProperties.getChatFileStoragePath();
    private final ChatMessageFileMapper messageFileMapper;

    public List<ChatMessageFile> save(List<ChatMessageFileDto> dtos) {
        List<ChatMessageFile> result = new ArrayList<>(dtos.size());
        try {
            for (ChatMessageFileDto dto : dtos) {
                result.add(save(dto));
            }
            return result;
        } catch (Exception exception) {
            delete(result);
            throw exception;
        }
    }

    public ChatMessageFile save(ChatMessageFileDto dto) {
        ChatMessageFile result = messageFileMapper.toEntity(dto);
        String uuidName;
        Path path;
        try {
            int i = 0;
            do {
                if (i++ >= 10) {
                    throw new ChatFileIoException("UUID could not be generated");
                }
                uuidName = UUID.randomUUID().toString();
                path = Paths.get(storagePath + "/" + uuidName);
            } while (Files.isRegularFile(path));
            result.setUuid(uuidName);
            Files.write(path, dto.getBytes());
        } catch (Exception e) {
            delete(result);
            throw new ChatFileIoException("File save error: " + result.getName());
        }
        return result;
    }

    public List<ChatMessageFileDto> get(List<ChatMessageFile> files) {
        List<ChatMessageFileDto> result = new ArrayList<>(files.size());
        for (ChatMessageFile file : files){
            result.add(get(file));
        }
        return result;
    }

    public ChatMessageFileDto get(ChatMessageFile file) {
        ChatMessageFileDto result;
        try {
            result = messageFileMapper.toDto(file);
            byte[] bytes = Files.readAllBytes(Paths.get(storagePath + "/" + file.getUuid()));
            result.setBytes(bytes);
        } catch (IOException e) {
            throw new ChatFileIoException("File load error: " + file.getName());
        }
        return result;
    }

    public void delete(ChatMessageFile file) {
        try {
            Files.delete(Paths.get(storagePath + "/" + file.getUuid()));
        } catch (IOException exception) {
            throw new ChatFileIoException("File delete error: " + file.getName());
        }
    }

    public void delete(List<ChatMessageFile> files){
        for (ChatMessageFile file : files){
            try {
                delete(file);
            } catch (Exception exception){
                log.error("File not deleted. UUID: " + file.getUuid(), exception);
            }
        }
    }
}
