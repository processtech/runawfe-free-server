package ru.runa.wfe.chat;

import org.json.simple.JSONObject;

/**
 * @author Sergey Inyakin
 */

public class JSONMessageStatus {

    private static final String MESS_TYPE = "messType";
    private static final String FILE_LOADED = "fileLoaded";
    private static final String NUMBER = "number";

    private static final String OK = "ok";
    private static final String ERROR = "error";
    private static final String STEP_LOAD_FILE = "stepLoadFile";
    private static final String NEXT_STEP_LOAD_FILE = "nextStepLoadFile";

    private static final String MESSAGE = "message";

    public static String nextStepLoadFile(boolean fileLoaded, int fileNumber){
        JSONObject result = getJsonObject();
        result.put(FILE_LOADED, fileLoaded);
        result.put(MESS_TYPE, NEXT_STEP_LOAD_FILE);
        result.put(NUMBER, fileNumber);
        return result.toJSONString();
    }

    public static String error(String massage){
        JSONObject result = getJsonObject();
        result.put(MESS_TYPE, ERROR);
        result.put(MESSAGE, massage);
        return result.toJSONString();
    }

    public static String ok(){
        JSONObject result = getJsonObject();
        result.put(MESS_TYPE, OK);
        return result.toJSONString();
    }

    public static String startLoadFiles(){
        JSONObject result = getJsonObject();
        result.put(MESS_TYPE, STEP_LOAD_FILE);
        return result.toJSONString();
    }

    private static JSONObject getJsonObject(){
        return new JSONObject();
    }
}
