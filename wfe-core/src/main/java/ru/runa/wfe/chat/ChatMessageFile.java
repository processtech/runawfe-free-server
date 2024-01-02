package ru.runa.wfe.chat;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class ChatMessageFile {

    public abstract Long getId();

    @Column(name = "FILE_NAME", length = 1024, nullable = false)
    private String name;

    @Column(name = "UUID", length = 36, nullable = false)
    private String uuid;

    public abstract ChatMessage getMessage();
}
