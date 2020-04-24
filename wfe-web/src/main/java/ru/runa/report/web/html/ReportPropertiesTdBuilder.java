package ru.runa.report.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.Permission;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class ReportPropertiesTdBuilder extends BaseTdBuilder {
    public ReportPropertiesTdBuilder() {
        super(Permission.READ);
    }

    @Override
    public TD build(Object object, Env env) {
        WfReport report = (WfReport) object;
        ConcreteElement startLink;

        if (isEnabled(object, env)) {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_REPORT, IdForm.ID_INPUT_NAME, report.getId(), env.getPageContext(),
                    PortletUrlType.Render);
            startLink = new A(url, MessagesCommon.LABEL_PROPERTIES.message(env.getPageContext()));
        } else {
            startLink = new StringElement();
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return MessagesCommon.LABEL_PROPERTIES.message(env.getPageContext());
    }
}
