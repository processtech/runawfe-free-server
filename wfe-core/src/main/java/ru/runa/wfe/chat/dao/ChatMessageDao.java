package ru.runa.wfe.chat.dao;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatMessageRecipient;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.user.Actor;

@Component
@MonitoredWithSpring
public class ChatMessageDao extends GenericDao<ChatMessage> {

    @Transactional(readOnly = true)
    @Override
    public ChatMessage get(Long id) {
        return super.get(id);
    }

    @Transactional(readOnly = true)
    public List<Long> getRecipientIdsByMessageId(Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.executor.id).from(cr).where(cr.message.id.eq(messageId)).fetch();
    }

    public void readMessage(Actor user, Long messageId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        Date date = new Date();
        queryFactory.update(cr).where(cr.executor.eq(user).and(cr.message.id.loe(messageId)).and(cr.readDate.isNull())).set(cr.readDate, date)
                .execute();
    }

    public List<ChatMessage> getMessages(Actor user, Long processId) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        return queryFactory.select(cr.message).from(cr)
                .where(cr.message.process.id.eq(processId).and(cr.executor.eq(user)))
                .orderBy(cr.message.createDate.desc()).fetch();
    }

    @Transactional(readOnly = true)
    public List<WfChatRoom> getChatRooms(Actor actor) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        QProcess p = QProcess.process;
        return queryFactory.select(Projections.constructor(WfChatRoom.class, p, cr.count().subtract(cr.readDate.count()))).from(cr)
                .join(cr.message.process, p).where(cr.executor.eq(actor))
                .groupBy(p.id).orderBy(p.id.desc()).fetch();
    }

    @Transactional(readOnly = true)
    public List<WfChatRoom> getOrderedChatRooms(Actor actor, List<Process> processes) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        QProcess p = QProcess.process;
        Map<Long, WfChatRoom> processIdToChatRoom = queryFactory.from(cr).join(cr.message.process, p)
                .where(cr.executor.eq(actor).and(p.in(processes))).groupBy(p.id).orderBy(p.id.desc())
                .transform(GroupBy.groupBy(p.id).as(Projections.constructor(WfChatRoom.class, p, cr.count().subtract(cr.readDate.count()))));
        List<WfChatRoom> rooms = new ArrayList<>(processes.size());
        for (Process process : processes) {
            rooms.add(processIdToChatRoom.get(process.getId()));
        }
        return rooms;
    }

    @Transactional
    public ChatMessage save(ChatMessage message, Set<Actor> recipients) {
        ChatMessage result = create(message);
        for (Actor recipient : recipients) {
            sessionFactory.getCurrentSession().save(new ChatMessageRecipient(message, recipient));
        }
        return result;
    }

    @Transactional
    @Override
    public ChatMessage update(ChatMessage entity) {
        return super.update(entity);
    }

    @Transactional
    public void deleteMessageAndRecipient(Long id) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        queryFactory.delete(cr).where(cr.message.id.eq(id)).execute();
        delete(id);
    }
}