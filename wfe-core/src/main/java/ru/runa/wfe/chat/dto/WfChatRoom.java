package ru.runa.wfe.chat.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * Created on 23.02.2021
 *
 * @author Sergey Inyakin
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class WfChatRoom extends SecuredObject {
    private WfProcess process;
    private Long newMessagesCount;

    public WfChatRoom(CurrentProcess process, String errors, Long newMessagesCount) {
        this.process = new WfProcess(process, errors);
        this.newMessagesCount = newMessagesCount;
    }

    @Override
    @EqualsAndHashCode.Include()
    public Long getId() {
        return process.getId();
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.CHAT_ROOMS;
    }
}
