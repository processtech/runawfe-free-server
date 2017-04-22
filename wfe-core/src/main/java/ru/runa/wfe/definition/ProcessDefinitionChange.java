package ru.runa.wfe.definition;

import java.io.Serializable;
import java.util.Date;

import com.google.common.base.Objects;

/**
 * Represents comment for process definition version.
 */
public class ProcessDefinitionChange implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long version;
    private Date date;
    private String author;
    private String comment;

    public ProcessDefinitionChange() {
    }

    public ProcessDefinitionChange(Date date, String author, String comment) {
        this.date = date;
        this.author = author != null ? author.intern() : null;
        this.comment = comment != null ? comment.intern() : null;
    }

    public ProcessDefinitionChange(Long version, ProcessDefinitionChange change) {
        this.version = version;
        this.date = change.getDate();
        this.author = change.getAuthor();
        this.comment = change.getComment();
    }

    /**
     * @return process definition version or <code>null</code> if unknown yet
     */
    public Long getVersion() {
        return this.version;
    }

    public Date getDate() {
        return this.date;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getComment() {
        return this.comment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessDefinitionChange) {
            // version is not used in equality
            ProcessDefinitionChange o = (ProcessDefinitionChange) obj;
            return Objects.equal(o.date, date) && Objects.equal(o.author, author) && Objects.equal(o.comment, comment);
        }
        return super.equals(obj);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(date, author, comment);
    }
}
