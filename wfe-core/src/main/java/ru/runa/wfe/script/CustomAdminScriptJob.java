package ru.runa.wfe.script;

import org.dom4j.Element;

import ru.runa.wfe.user.User;

public interface CustomAdminScriptJob {

    public void execute(User user, Element element) throws Exception;

}
