package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;

public class FunctionDef {
    private final String className;
    private final String label;
    private final List<ParamDef> params = new ArrayList<ParamDef>();

    public FunctionDef(String className, String label) {
        this.className = className;
        this.label = label;
    }

    public void addParam(ParamDef definition) {
        params.add(definition);
    }

    public String getLabel() {
        return label;
    }

    public String getClassName() {
        return className;
    }

    public List<ParamDef> getParams() {
        return params;
    }
}
