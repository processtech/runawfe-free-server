package ru.runa.wfe.digitalsignature;

import com.google.common.base.MoreObjects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.security.IdBasedSecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@Entity
@Table(name = "DIGITAL_SIGNATURE")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@EqualsAndHashCode
public class DigitalSignature extends IdBasedSecuredObject {
    private static final long serialVersionUID = 1L;

    @Transient
    private long MILLISECONDS_IN_MONTH = 31556952000L / 12;

    @Transient
    private boolean containerValid;

    public DigitalSignature() {
        commonName = "";
        email = "";
        department = "";
        organization = "";
        city = "";
        state = "";
        country = "RU";
        validityInMonth = 12;
        dateOfIssue = new Date();
        dateOfExpiry = new Date(dateOfIssue.getTime() + validityInMonth * MILLISECONDS_IN_MONTH);
    }

    public DigitalSignature(String commonName, String email, String department, String organization, String city,
            String state, String country, Integer validityInMonth, Long actorId) {
        this.commonName = commonName;
        this.email = email;
        this.department = department;
        this.organization = organization;
        this.city = city;
        this.state = state;
        this.country = country;
        this.validityInMonth = validityInMonth;
        this.actorId = actorId;
        dateOfIssue = new Date();
        dateOfExpiry = new Date(dateOfIssue.getTime() + validityInMonth * MILLISECONDS_IN_MONTH);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_DIGITAL_SIGNATURE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ACTOR_ID")
    private Long actorId;

    @Lob
    @Column(name = "CONTAINER")
    private byte[] container;

    @Transient
    private Date dateOfIssue;

    @Transient
    private Date dateOfExpiry;

    @Transient
    private String commonName;

    @Transient
    private String email;

    @Transient
    private String department;

    @Transient
    private String organization;

    @Transient
    private String city;

    @Transient
    private String state;

    @Transient
    private String country;

    @Transient
    private Integer validityInMonth;

    @Transient
    private DigitalSignature rootDS;

    @Transient
    public boolean isDigitalSignatureValid() {
        Date date = new Date();
        return (getContainer() != null) && date.before(getDateOfExpiry());
    }

    @Transient
    public void setIssueAndExpiryDate() {
        dateOfIssue = new Date();
        dateOfExpiry = new Date(dateOfIssue.getTime() + validityInMonth * MILLISECONDS_IN_MONTH);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getActorId()).add("name", getCommonName()).toString();
    }

    @Transient
    public String getLabel() {
        return commonName;
    }

    @Override
    @Transient
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DIGITAL_SIGNATURE;
    }

    @Transient
    public void countValidity() {
        validityInMonth = Math.toIntExact((dateOfExpiry.getTime() - dateOfIssue.getTime()) / MILLISECONDS_IN_MONTH);
    }

}
