package ru.runa.wfe.report.dto;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ru.runa.wfe.commons.Categorized;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.security.IdBasedSecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@Getter
@Setter
public class WfReport extends IdBasedSecuredObject implements Comparable<WfReport>, Categorized {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String category;
    private byte[] compiledReport;
    private List<WfReportParameter> parameters;

    public WfReport() {
    }

    public WfReport(Long id, String name, String description, String category, List<WfReportParameter> parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.parameters = parameters;
    }

    public WfReport(ReportDefinition definition) {
        id = definition.getId();
        name = definition.getName();
        description = definition.getDescription();
        category = definition.getCategory();
        compiledReport = definition.getCompiledReport();
        parameters = new ArrayList<>(Lists.transform(definition.getParameters(),
                new Function<ReportParameter, WfReportParameter>() {
                    int position = 0;

                    @Override
                    public WfReportParameter apply(ReportParameter input) {
                        return new WfReportParameter(input.getName(), "", input.getInnerName(), ++position, input.getType(), input.isRequired());
                    }
                }));
    }

    @Override
    public int compareTo(WfReport arg0) {
        return name.compareTo(arg0.getName());
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.REPORT;
    }

    public byte[] getCompiledReport() {
        return compiledReport;
    }
}
