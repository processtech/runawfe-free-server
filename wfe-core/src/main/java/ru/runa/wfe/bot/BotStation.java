package ru.runa.wfe.bot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BOT_STATION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class BotStation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BOT_STATION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "VERSION")
    private Long version;

    @Column(name = "NAME", unique = true, nullable = false, length = 1024)
    private String name;

    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;

    public BotStation() {
    }

    public BotStation(String name) {
        this.name = name;
        this.version = 0L;
        this.createDate = new Date();
    }

    public BotStation(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BotStation) {
            BotStation b = (BotStation) obj;
            return Objects.equal(name, b.name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }
}
