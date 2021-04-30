package ru.runa.wfe.statistics;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

@Entity
@Table(name = "STATISTIC_REPORT_LOG")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StatisticReportLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String version;
    private String uuid;
    private Date createDate;
    private Boolean successExecution;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_STATISTIC_REPORT_LOG", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VERSION", nullable = false)
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Column(name = "UUID", nullable = false)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "IS_SUCCESS_EXECUTION")
    public Boolean isSuccessExecution() {
        return successExecution;
    }

    public void setSuccessExecution(Boolean successExecution) {
        this.successExecution = successExecution == null ? Boolean.FALSE : successExecution;
    }
}
