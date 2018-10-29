package ru.runa.common.web.html;

import ru.runa.wfe.user.User;

public interface CssClassStrategy {
    String getClassName(Object item, User user);

    String getCssStyle(Object item);
}
