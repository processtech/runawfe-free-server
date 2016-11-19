package ru.runa.wfe.script;

import java.util.Map;

import ru.runa.wfe.user.User;

public class AdminScriptData {
    private User user;
    private byte[][] processDefinitionsBytes;
    protected Map<String, byte[]> configs;
    private String defaultPasswordValue;

}
