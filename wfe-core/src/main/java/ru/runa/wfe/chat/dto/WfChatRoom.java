package ru.runa.wfe.chat.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@XmlAccessorType(XmlAccessType.FIELD)
public class WfChatRoom extends SecuredObject {
    private Long id;
    private String processName;
    private Long newMessagesCount;

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.CHAT_ROOMS;
    }
}
