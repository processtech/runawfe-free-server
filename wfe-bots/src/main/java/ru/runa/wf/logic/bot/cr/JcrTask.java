package ru.runa.wf.logic.bot.cr;

public class JcrTask {
    public static final String REMOVE_FILE = "remove";
    public static final String PUT_FILE = "put";
    public static final String GET_FILE = "get";

    private final String operationName;
    private final String variableName;
    private final String fileName;
    private final String path;

    public JcrTask(String operationName, String variableName, String path, String fileName) {
        this.operationName = operationName;
        this.variableName = variableName;
        this.path = path;
        this.fileName = fileName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

}
