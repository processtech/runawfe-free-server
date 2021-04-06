package ru.runa.common.web.html;

import java.util.List;

import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Resources;

/**
 */
public class StringsHeaderBuilder implements HeaderBuilder {
    private String[] headers;

    public StringsHeaderBuilder(String[] headers) {
        setHeaders(headers);
    }

    public StringsHeaderBuilder(List<String> headers) {
        setHeaders(headers.toArray(new String[headers.size()]));
    }

    protected StringsHeaderBuilder() {
    }

    protected void setHeaders(String[] headers) {
        this.headers = headers.clone();
    }

    @Override
    public TR build() {
        if (headers == null) {
            throw new IllegalStateException("headers weren't initialized");
        }
        TR tr = new TR();
        for (int i = 0; i < headers.length; i++) {
            tr.addElement(new TH(headers[i]).setClass(Resources.CLASS_LIST_TABLE_TH));
        }
        return tr;
    }
}
