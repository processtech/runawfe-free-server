package ru.runa.wfe.chat;

import java.io.File;
import javax.annotation.PostConstruct;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;

@Component
@CommonsLog
public class ChatInitializerLogic {

    @PostConstruct
    private void createChatFilesFolder() {
        final String chatFilesStoragePath = SystemProperties.getChatFileStoragePath();
        if (new File(chatFilesStoragePath).mkdir()) {
            log.info("Chat file storage folder has been created " + chatFilesStoragePath);
        }
    }
}