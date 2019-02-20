package ru.runa.wfe.commons.error;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class SystemError implements Serializable, Comparable<SystemError> {
    private static final long serialVersionUID = 1L;
    private Date occurredDate;
    private String message;
    private String stackTrace;

    public SystemError() {
        occurredDate = new Date();
    }

    public SystemError(Throwable throwable) {
        this();
        setThrowable(throwable);
    }

    public Date getOccurredDate() {
        return occurredDate;
    }

    public void setOccurredDate(Date occurredDate) {
        this.occurredDate = occurredDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setThrowable(Throwable throwable) {
        if (throwable != null) {
            this.message = throwable.toString();
            this.stackTrace = Throwables.getStackTraceAsString(throwable);
        }
    }

    @Override
    public int compareTo(SystemError o) {
        // desc
        return o.occurredDate.compareTo(occurredDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SystemError) {
            SystemError o = (SystemError) obj;
            return Objects.equal(message, o.message);
        }
        return super.equals(obj);
    }

}
