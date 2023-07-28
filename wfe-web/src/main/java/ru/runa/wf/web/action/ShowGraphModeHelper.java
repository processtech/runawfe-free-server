package ru.runa.wf.web.action;

import ru.runa.common.WebResources;

public class ShowGraphModeHelper {

    /**
     * Used from JSP.
     */
    public static boolean isShowGraphMode() {
        return WebResources.isShowGraphMode();
    }

    public static String getManageProcessAction() {
        if (isShowGraphMode()) {
            return WebResources.ACTION_SHOW_PROCESS_GRAPH;
        } else {
            return WebResources.ACTION_MAPPING_MANAGE_PROCESS;
        }
    }
}
