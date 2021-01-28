package ru.runa.wfe.chat.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatFileIoException;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;

/**
 * @author Sergey Inyakin
 */
@Component
@PropertySource("classpath:system.properties")
public class ChatFileIo {

    @Value("${chat.files.storage.path}")
    private String storagePath;
    @Autowired
    private ChatMessageFileMapper messageFileMapper;

    public List<ChatMessageFile> write(List<ChatMessageFileDto> dtos) {
        List<ChatMessageFile> result = new ArrayList<>();
        try {
            for (ChatMessageFileDto dto : dtos)
                result.add(write(dto));
        } catch (Exception exception) {
            delete(result);
            throw exception;
        }
        return result;
    }

    public ChatMessageFile write(ChatMessageFileDto dto) {
        ChatMessageFile result = messageFileMapper.toEntity(dto);
        String uuidName;
        Path path;
        do {
            uuidName = UUID.randomUUID().toString();
            path = Paths.get(storagePath + "/" + uuidName);
        } while (Files.isRegularFile(path));
        try {
            result.setUuid(uuidName);
            Files.write(path, dto.getBytes());
        } catch (Exception e) {
            delete(result);
            throw new ChatFileIoException("File save error: " + result.getFileName());
        }
        return result;
    }

    public List<ChatMessageFileDto> read(List<ChatMessageFile> files) {
        List<ChatMessageFileDto> result = new ArrayList<>();
        for (ChatMessageFile file : files){
            read(file);
        }
        return result;
    }

    public ChatMessageFileDto read(ChatMessageFile file) {
        ChatMessageFileDto result;
        try {
            result = messageFileMapper.toDto(file);
            byte[] bytes = Files.readAllBytes(Paths.get(storagePath + "/" + file.getUuid()));
            result.setBytes(bytes);
        } catch (IOException e) {
            throw new ChatFileIoException("File load error: " + file.getFileName());
        }
        return result;
    }

    public void delete(ChatMessageFile file) {
        try {
            Files.delete(Paths.get(storagePath + "/" + file.getUuid()));
        } catch (IOException exception) {
            throw new ChatFileIoException("File delete error: " + file.getFileName());
        }
    }

    public void delete(List<ChatMessageFile> files){
        for (ChatMessageFile file : files){
            delete(file);
        }
    }
}
