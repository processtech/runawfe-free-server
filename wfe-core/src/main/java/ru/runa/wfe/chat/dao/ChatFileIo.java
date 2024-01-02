package ru.runa.wfe.chat.dao;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatFileIoException;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.chat.mapper.ChatMessageFileMapper;
import ru.runa.wfe.commons.SystemProperties;

/**
 * @author Sergey Inyakin
 */
@CommonsLog
@Component
@MonitoredWithSpring
public class ChatFileIo {
    private final String storagePath = SystemProperties.getChatFileStoragePath();
    @Autowired
    private ChatMessageFileMapper messageFileMapper;

    public List<CurrentChatMessageFile> save(List<ChatMessageFileDto> dtos) {
        List<CurrentChatMessageFile> result = new ArrayList<>(dtos.size());
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

    public CurrentChatMessageFile save(ChatMessageFileDto dto) {
        CurrentChatMessageFile result = messageFileMapper.toEntity(dto);
        try {
            Path path = generateUuidPath();
            result.setUuid(path.getFileName().toString());
            Files.write(path, dto.getBytes());
        } catch (Exception e) {
            delete(result);
            throw new ChatFileIoException("File save error: " + result.getName());
        }
        return result;
    }

    public List<ChatMessageFileDto> get(List<ChatMessageFile> files) {
        List<ChatMessageFileDto> result = new ArrayList<>(files.size());
        for (ChatMessageFile file : files) {
            result.add(get(file));
        }
        return result;
    }

    public ChatMessageFileDto get(ChatMessageFile file) {
        try {
            final ChatMessageFileDto result = messageFileMapper.toDto(file);
            byte[] bytes = Files.readAllBytes(Paths.get(storagePath + "/" + file.getUuid()));
            result.setBytes(bytes);
            return result;
        } catch (IOException e) {
            throw new ChatFileIoException("File load error: " + file.getName());
        }
    }

    public void delete(CurrentChatMessageFile file) {
        try {
            Files.delete(Paths.get(storagePath + "/" + file.getUuid()));
        } catch (IOException exception) {
            throw new ChatFileIoException("File delete error: " + file.getName());
        }
    }

    public void delete(List<CurrentChatMessageFile> files) {
        for (CurrentChatMessageFile file : files) {
            try {
                delete(file);
            } catch (Exception exception) {
                log.error("File not deleted. UUID: " + file.getUuid(), exception);
            }
        }
    }

    public Stream<Path> getFilesOlderThan(ZonedDateTime olderThan) throws IOException {
        final BiPredicate<Path, BasicFileAttributes> filePredicate =
                new BiPredicate<Path, BasicFileAttributes>() {
                    @Override
                    public boolean test(Path path, BasicFileAttributes basicFileAttributes) {
                        return basicFileAttributes.creationTime().toInstant().isBefore(olderThan.toInstant()) && Files.isRegularFile(path);
                    }
                };
        return Files.find(Paths.get(storagePath), 1, filePredicate);
    }

    public void deleteByUuid(String uuid) {
        try {
            Files.delete(Paths.get(storagePath + "/" + uuid));
        } catch (IOException e) {
            throw new ChatFileIoException("Unable delete file by UUID " + uuid);
        }
    }

    private Path generateUuidPath() {
        String uuidName;
        Path result;
        int i = 0;
        do {
            if (i++ >= 10) {
                throw new ChatFileIoException("UUID could not be generated");
            }
            uuidName = UUID.randomUUID().toString();
            result = Paths.get(storagePath + "/" + uuidName);
        } while (Files.isRegularFile(result));
        return result;
    }
}
