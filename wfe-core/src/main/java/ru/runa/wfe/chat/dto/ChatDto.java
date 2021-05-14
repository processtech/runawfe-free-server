package ru.runa.wfe.chat.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class ChatDto implements Serializable {
    private static final long serialVersionUID = -4017045271560463046L;

    private Long processId;
}
