package ru.runa.common.web.html;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Resources;

public class RadioButtonTdBuilder extends BaseTdBuilder {
    private final String inputName;
    private final String propertyName;

    public RadioButtonTdBuilder(String inputName, String propertyName) {
        super(null);
        this.inputName = inputName;
        this.propertyName = propertyName;
    }

    @Override
    public TD build(Object object, Env env) {
        Input input = new Input(Input.RADIO, inputName, getValue(object, env));
        TD td = new TD(input);
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return readProperty(object, propertyName, true);
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
