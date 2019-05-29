package ru.runa.wfe.validation;

import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorConfig {
    private final String type;
    private final List<String> transitionNames = Lists.newArrayList();
    private final Map<String, String> params = Maps.newHashMap();
    private String message;

    public ValidatorConfig(String type) {
        this.type = type;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getType() {
        return type;
    }

    public List<String> getTransitionNames() {
        return transitionNames;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("params", params).toString();
    }
}
