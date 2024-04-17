package ru.runa.wfe.execution.process.check;

import java.io.Serializable;
import lombok.Data;
import lombok.NonNull;

@Data
public class FrozenProcessSearchData implements Serializable {

    private static final long serialVersionUID = -208336828839876993L;

    @NonNull
    private String seekerId;

    private int timeValue;
}
