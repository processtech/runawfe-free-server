package ru.runa.wfe.chat.dao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageFile;
import ru.runa.wfe.chat.QChatMessageFile;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.dto.ChatFileDto;
import ru.runa.wfe.chat.dto.ChatMessageFileDto;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * @author Sergey Inyakin
 */
@Component
public class ChatFileDao extends GenericDao<ChatMessageFile> {

    @Autowired
    private ChatFileIo chatFileIo;

    public List<ChatFileDto> saveAllFiles(List<ChatMessageFileDto> dtos, ChatMessage message){
        List<ChatMessageFile> files = chatFileIo.write(dtos);

        //Пересмотреть DTO
        List<ChatFileDto> result = new ArrayList<>();
        for (ChatMessageFile file : files) {
            file.setMessage(message);
            sessionFactory.getCurrentSession().save(file);
            result.add(new ChatFileDto(file.getId(), file.getFileName()));
        }
        return result;
    }

    public List<ChatMessageFileDto> getMessageFiles(Actor actor, ChatMessage message) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        List<ChatMessageFile> files = queryFactory.select(mf).from(mf, cr)
                .where(mf.message.eq(message).and(cr.message.eq(mf.message)).and(cr.executor.eq(actor))).fetch();
        return chatFileIo.read(files);
    }

    public ChatMessageFileDto getFile(Actor actor, Long fileId) {
        QChatMessageFile mf = QChatMessageFile.chatMessageFile;
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return chatFileIo.read(queryFactory.select(mf).from(mf, cr).where(mf.id.eq(fileId).and(mf.message.eq(cr.message).and(cr.executor.eq(actor))))
                .fetchFirst());
    }

    public ChatMessageFileDto saveFile(ChatMessageFileDto dto) {
        sessionFactory.getCurrentSession().save(chatFileIo.write(dto));
        return dto;
    }

    public void deleteFile(User user, Long id) {
        QChatMessageFile f = QChatMessageFile.chatMessageFile;
        queryFactory.delete(f).where(f.id.eq(id)).execute();
        chatFileIo.delete(queryFactory.select(f).from(f).where(f.id.eq(id)).fetchFirst());
    }

}
