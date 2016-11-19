package ru.runa.wfe.commons.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.user.User;

public interface AjaxCommand {

    public void execute(User user, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
