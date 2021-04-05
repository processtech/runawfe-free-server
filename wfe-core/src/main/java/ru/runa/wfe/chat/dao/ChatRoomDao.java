package ru.runa.wfe.chat.dao;

import com.querydsl.core.types.Projections;
import net.bull.javamelody.MonitoredWithSpring;
import org.springframework.stereotype.Component;
import ru.runa.wfe.chat.ChatRoom;
import ru.runa.wfe.chat.QChatMessageRecipient;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.user.Actor;
import java.util.List;

@Component
@MonitoredWithSpring
public class ChatRoomDao extends GenericDao<ChatRoom> {

    public List<WfChatRoom> getChatRooms(Actor actor) {
        QChatMessageRecipient cr = QChatMessageRecipient.chatMessageRecipient;
        QProcess p = QProcess.process;
        return queryFactory.select(Projections.constructor(WfChatRoom.class, p, cr.count().subtract(cr.readDate.count()))).from(cr)
                .join(cr.message.process, p).where(cr.executor.eq(actor))
                .groupBy(p.id).orderBy(p.id.desc()).fetch();
    }
}
