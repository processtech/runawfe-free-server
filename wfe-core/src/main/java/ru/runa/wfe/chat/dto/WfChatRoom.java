package ru.runa.wfe.chat.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;
import java.util.Date;

/**
 * Created on 23.02.2021
 *
 * @author Sergey Inyakin
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class WfChatRoom extends SecuredObjectBase {
    private Long id;
    private String processName;
    private Date startDate;
    private Date endDate;
    private int version;
    private Long newMessagesCount;

    public WfChatRoom(Process process, Long newMessagesCount) {
        this.id = process.getId();
        this.processName = process.getDeployment().getName();
        this.startDate = process.getStartDate();
        this.endDate = process.getEndDate();
        this.version = process.getDeployment().getVersion().intValue();
        this.newMessagesCount = newMessagesCount;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.CHAT_ROOMS;
    }
}
