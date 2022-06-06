package ru.runa.wfe.chat;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.dao.ChatFileDao;
import ru.runa.wfe.chat.dao.ChatFileIo;

/**
 * @author Alekseev Mikhail
 * @since #2199
 */
@CommonsLog
@MonitoredWithSpring
public class ChatUnusedFilesCleanerJob {
    @Value("${chat.unused.filed.cleaner.older.than.millis}")
    private long olderThanMillis;
    @Autowired
    private ChatFileIo fileIo;
    @Autowired
    private ChatFileDao fileDao;
    @Resource(name = "chatUnusedFilesCleanerJob")
    private ChatUnusedFilesCleanerJob self;

    @SneakyThrows
    @Scheduled(fixedDelayString = "${timertask.period.millis.clean.unused.chat.files}")
    public void execute() {
        log.debug("Job started");

        fileIo.getFilesOlderThan(ZonedDateTime.now().minusNanos(olderThanMillis * 1000))
                .map(new MapPathToFileName())
                .filter(new UuidFilter(self.getUsedFileUuids()))
                .forEach(new DeleteFileConsumer(fileIo));

        log.debug("Job ended");
    }

    @Transactional(readOnly = true)
    public Set<String> getUsedFileUuids() {
        return new HashSet<>(fileDao.getAllFileUuids());
    }

    @RequiredArgsConstructor
    private static final class UuidFilter implements Predicate<String> {
        private final Set<String> usedFileUuids;

        @Override
        public boolean test(String uuid) {
            return !usedFileUuids.contains(uuid);
        }
    }

    @RequiredArgsConstructor
    private static final class DeleteFileConsumer implements Consumer<String> {
        private final ChatFileIo fileIo;

        @Override
        public void accept(String uuid) {
            log.info("Deleting unused chat file " + uuid);
            try {
                fileIo.deleteByUuid(uuid);
            } catch (Exception e) {
                log.warn("Unable to delete unused chat file " + uuid);
            }
        }
    }

    private static final class MapPathToFileName implements Function<Path, String> {
        @Override
        public String apply(Path path) {
            return path.getFileName().toString();
        }
    }
}
