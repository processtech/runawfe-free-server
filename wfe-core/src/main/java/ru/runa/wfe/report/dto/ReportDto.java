package ru.runa.wfe.report.dto;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.EntityWithType;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ReportDto extends Identifiable implements Comparable<ReportDto>, EntityWithType {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private String category;
    private byte[] compiledReport;
    private List<ReportParameterDto> parameters;

    public ReportDto() {
        super();
    }

    public ReportDto(Long id, String name, String description, String category, List<ReportParameterDto> parameters) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.parameters = parameters;
    }

    public ReportDto(ReportDefinition definition) {
        super();
        id = definition.getId();
        name = definition.getName();
        description = definition.getDescription();
        category = definition.getCategory();
        compiledReport = definition.getCompiledReport();
        parameters = new ArrayList<ReportParameterDto>(Lists.transform(definition.getParameters(),
                new Function<ReportParameter, ReportParameterDto>() {
                    int position = 0;

                    @Override
                    public ReportParameterDto apply(ReportParameter input) {
                        return new ReportParameterDto(input.getName(), "", input.getInnerName(), ++position, input.getType(), input.isRequired());
                    }
                }));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String[] getCategories() {
        if (category != null) {
            return category.split(Utils.CATEGORY_DELIMITER);
        }
        return new String[] {};
    }

    public List<ReportParameterDto> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportParameterDto> parameters) {
        this.parameters = parameters;
    }

    @Override
    public int compareTo(ReportDto arg0) {
        return name.compareTo(arg0.getName());
    }

    @Override
    public Long getIdentifiableId() {
        return getId();
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.REPORT;
    }

    public byte[] getCompiledReport() {
        return compiledReport;
    }
}
