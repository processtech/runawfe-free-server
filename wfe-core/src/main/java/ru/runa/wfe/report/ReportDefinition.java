package ru.runa.wfe.report;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * DTO for report description.
 */

@Entity
@Table(name = "REPORT", indexes = { @Index(name = "IX_REPORT_NAME", unique = true, columnList = "NAME") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportDefinition {
    private Long id;
    private Long version;
    /**
     * Report name that is shown to user.
     */
    private String name;

    /**
     * Report description that is shown to user.
     */
    private String description;

    /**
     * Parameters set that is required for user to input in order to build report.
     */
    private List<ReportParameter> parameters;

    /**
     * Compiled (.jasper) jasper reports.
     */
    private byte[] compiledReport;

    /**
     * Configuration and report construction type.
     */
    private ReportConfigurationType configType;

    /**
     * Report category (type).
     */
    private String category;

    public ReportDefinition() {
    }

    public ReportDefinition(Long id, String name, String description, byte[] compiledReport, List<ReportParameter> parameters, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.compiledReport = compiledReport;
        this.configType = ReportConfigurationType.RAW_SQL_REPORT;
        this.parameters = new ArrayList<>(parameters);
        this.category = category;
        this.version = 1L;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_REPORT", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME", length = 1024, nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String reportName) {
        this.name = reportName;
    }

    @Column(name = "DESCRIPTION", length = 2048)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(mappedBy = "definition", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "IDX")
    public List<ReportParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportParameter> reportParameters) {
        this.parameters = reportParameters;
    }

    @Lob
    @Column(name = "COMPILED_REPORT", nullable = false, length = 128 * 1024 * 1024)
    public byte[] getCompiledReport() {
        return compiledReport;
    }

    public void setCompiledReport(byte[] compiledReport) {
        this.compiledReport = compiledReport;
    }

    @Column(name = "CONFIG_TYPE", length = 1024, nullable = false)
    @Enumerated(EnumType.STRING)
    public ReportConfigurationType getConfigType() {
        return configType;
    }

    public void setConfigType(ReportConfigurationType reportConfigType) {
        this.configType = reportConfigType;
    }

    @Column(name = "CATEGORY", length = 1024)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategory(List<String> category) {
        this.category = Joiner.on('/').join(category);
    }

    @Transient
    public String getConfigTypeDescription() {
        return configType.getDescription();
    }

    public void updateFrom(ReportDefinition reportDefinition) {
        this.category = reportDefinition.category;
        this.compiledReport = reportDefinition.compiledReport;
        this.configType = reportDefinition.configType;
        this.description = reportDefinition.description;
        this.name = reportDefinition.name;
        this.parameters.clear();
        this.parameters.addAll(reportDefinition.getParameters());
    }
}
