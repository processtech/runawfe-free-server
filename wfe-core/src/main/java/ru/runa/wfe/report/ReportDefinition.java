package ru.runa.wfe.report;

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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.google.common.base.Joiner;

/**
 * DTO для описания отчета.
 */

@Entity
@Table(name = "REPORT", indexes = { @Index(name = "IX_REPORT_NAME", unique = true, columnList = "NAME") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ReportDefinition {
    private Long id;
    private Long version;
    /**
     * Название отчета, отображаемое пользователю.
     */
    private String name;

    /**
     * Описание отчета, отображаемое пользователю.
     */
    private String description;

    /**
     * Набор параметров, которые требуется запросить у пользователя для
     * построения отчета.
     */
    private List<ReportParameter> parameters;

    /**
     * Скомпилированный (.jasper) отчет jasper reports.
     */
    private byte[] compiledReport;

    /**
     * Тип конфигурации и построения отчета.
     */
    private ReportConfigurationType configType;

    /**
     * Содержимое JAR файла, требуемого для построения отчета. Используется для
     * построения отчетов с типом конфигурации
     * {@link ReportConfigurationType.PARAMETER_BUILDER}.
     */
    private byte[] jarFile;

    /**
     * Название класса, реализующего интерфейс {@link ReportParametersBuilder} и
     * используемого для заполнения параметров отчета. Используется для
     * построения отчетов с типом конфигурации
     * {@link ReportConfigurationType.PARAMETER_BUILDER}.
     */
    private String parameterBuilderClassName;

    /**
     * Категория (тип) отчета.
     */
    private String category;

    public ReportDefinition() {
        version = 1L;
        parameters = new ArrayList<ReportParameter>();
    }

    public ReportDefinition(Long id, String name, String description, byte[] compiledReport, List<ReportParameter> parameters, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.compiledReport = compiledReport;
        this.configType = ReportConfigurationType.RAW_SQL_REPORT;
        this.parameters = new ArrayList<ReportParameter>(parameters);
        this.category = category;
        version = 1L;
        parameters = new ArrayList<ReportParameter>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_REPORT", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Column(name = "DESCRIPTION", length = 2048, nullable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany(targetEntity = ReportParameter.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "REPORT_ID", nullable = false)
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

    @Lob
    @Column(name = "JAR_FILE", nullable = true, length = 128 * 1024 * 1024)
    public byte[] getJarFile() {
        return jarFile;
    }

    public void setJarFile(byte[] jarFile) {
        this.jarFile = jarFile;
    }

    @Column(name = "PARAM_BUILDER_NAME", length = 1024, nullable = true)
    public String getParameterBuilderClassName() {
        return parameterBuilderClassName;
    }

    public void setParameterBuilderClassName(String parameterBuilderClassName) {
        this.parameterBuilderClassName = parameterBuilderClassName;
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
        this.jarFile = reportDefinition.jarFile;
        this.name = reportDefinition.name;
        this.parameterBuilderClassName = reportDefinition.parameterBuilderClassName;
        this.parameters.clear();
        this.parameters.addAll(reportDefinition.getParameters());
    }
}
