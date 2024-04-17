package ru.runa.wfe.chat.dao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.bull.javamelody.MonitoredWithSpring;
import org.apache.commons.collections.ListUtils;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ArchivedChatMessage;
import ru.runa.wfe.chat.ArchivedChatMessageFile;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.CurrentChatMessage;
import ru.runa.wfe.chat.CurrentChatMessageFile;
import ru.runa.wfe.chat.QArchivedChatMessageFile;
import ru.runa.wfe.chat.QCurrentChatMessageFile;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * @author Sergey Inyakin
 */

@Component
@MonitoredWithSpring
public class ChatFileDao extends GenericDao<ChatMessageFile> {

    public ChatFileDao() {
        super(ChatMessageFile.class);
    }

    public List<CurrentChatMessageFile> save(List<CurrentChatMessageFile> files) {
        for (ChatMessageFile file : files) {
            sessionFactory.getCurrentSession().save(file);
        }
        return files;
    }

    @Override
    public ChatMessageFile get(Long id) {
        return get(CurrentChatMessageFile.class, id);
    }

    public ChatMessageFile getFromArchive(Long id) {
        return get(ArchivedChatMessageFile.class, id);
    }

    public List<CurrentChatMessageFile> getByMessage(ChatMessage message) {
        QCurrentChatMessageFile mf = QCurrentChatMessageFile.currentChatMessageFile;
        return queryFactory.select(mf).from(mf).where(mf.message.id.eq(message.getId())).fetch();
    }

    public long deleteByMessage(CurrentChatMessage message) {
        QCurrentChatMessageFile mf = QCurrentChatMessageFile.currentChatMessageFile;
        return queryFactory.delete(mf).where(mf.message.id.eq(message.getId())).execute();
    }

    public long deleteArchivedByMessage(ArchivedChatMessage message) {
        QArchivedChatMessageFile mf = QArchivedChatMessageFile.archivedChatMessageFile;
        return queryFactory.delete(mf).where(mf.message.id.eq(message.getId())).execute();
    }

    public List<ArchivedChatMessageFile> getByMessageFromArchive(ChatMessage message) {
        QArchivedChatMessageFile mf = QArchivedChatMessageFile.archivedChatMessageFile;
        return queryFactory.select(mf).from(mf).where(mf.message.id.eq(message.getId())).fetch();
    }

    public List<String> getAllFileUuids() {
        final QCurrentChatMessageFile f = QCurrentChatMessageFile.currentChatMessageFile;
        final QArchivedChatMessageFile af = QArchivedChatMessageFile.archivedChatMessageFile;
        return Stream.of(queryFactory.select(f.uuid).from(f).fetch(), queryFactory.select(af.uuid).from(af).fetch())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
