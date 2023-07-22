package ru.runa.wfe.var.format;

import ru.runa.wfe.user.Group;

public class GroupFormat extends ExecutorFormat {

    @Override
    public Class<? extends Group> getJavaClass() {
        return Group.class;
    }

}
