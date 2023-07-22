package ru.runa.wf.web.html;

import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;

public class HistoryHeaderBuilder implements HeaderBuilder {
    private final int subprocessLevel;
    private final String dateLabel;
    private final String eventLabel;

    public HistoryHeaderBuilder(int subprocessLevel, String dateLabel, String eventLabel) {
        this.subprocessLevel = subprocessLevel;
        this.dateLabel = dateLabel;
        this.eventLabel = eventLabel;
    }

    @Override
    public TR build() {
        TR tr = new TR();
        for (int i = 0; i < subprocessLevel; i++) {
            tr.addElement(new TH("").setClass(Resources.CLASS_EMPTY20_TABLE_TD));
        }
        tr.addElement(new TH(dateLabel).setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(eventLabel).setClass(Resources.CLASS_LIST_TABLE_TH));
        return tr;
    }

}
