package ru.runa.wfe.execution;

import java.io.Serializable;
import java.util.Date;

/**
 * Filter for process search.
 * 
 * @author Dofs
 * @since 4.0
 */
public class ProcessFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String definitionName;
    private String definitionVersion;
    private Long id;
    private Long idFrom;
    private Long idTo;
    private Date startDateFrom;
    private Date startDateTo;
    private Boolean finished;
    private Date endDateFrom;
    private Date endDateTo;
    private boolean failedOnly;

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getDefinitionVersion() {
        return definitionVersion;
    }

    public void setDefinitionVersion(String version) {
        this.definitionVersion = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public Date getStartDateFrom() {
        return startDateFrom;
    }

    public void setStartDateFrom(Date startDateFrom) {
        this.startDateFrom = startDateFrom;
    }

    public Date getStartDateTo() {
        return startDateTo;
    }

    public void setStartDateTo(Date startDateTill) {
        this.startDateTo = startDateTill;
    }

    public Date getEndDateFrom() {
        return endDateFrom;
    }

    public void setEndDateFrom(Date endDateFrom) {
        this.endDateFrom = endDateFrom;
    }

    public Date getEndDateTo() {
        return endDateTo;
    }

    public void setEndDateTo(Date endDateTill) {
        this.endDateTo = endDateTill;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public boolean getFailedOnly() {
        return failedOnly;
    }

    public void setFailedOnly(boolean failedOnly) {
        this.failedOnly = failedOnly;
    }

}
