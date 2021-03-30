package ru.runa.wfe.chat.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.var.dto.WfVariable;

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
public class WfChatRoom extends SecuredObjectBase {
    private WfProcess process;
    private Long newMessagesCount;

    public WfChatRoom(Process process, Long newMessagesCount) {
        this.process = new WfProcess(process, "");
        this.newMessagesCount = newMessagesCount;
    }

    public void addVariable(WfVariable variable) {
        process.addVariable(variable);
    }

    public WfVariable getVariable(String name) {
        return process.getVariable(name);
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.CHAT_ROOMS;
    }

    @Override
    @EqualsAndHashCode.Include()
    public Long getId() {
        return process.getId();
    }
}
