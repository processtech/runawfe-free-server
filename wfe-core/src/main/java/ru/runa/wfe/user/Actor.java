package ru.runa.wfe.user;

import com.google.common.base.MoreObjects;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * Actor represents a real user of system that could perform different actions.
 */
@Entity
@DiscriminatorValue(value = Actor.DISCRIMINATOR_VALUE)
public class Actor extends Executor {
    private static final long serialVersionUID = -582492651083909598L;
    public static final String DISCRIMINATOR_VALUE = "N";
    public static final Actor UNAUTHORIZED_ACTOR = new Actor(UNAUTHORIZED_EXECUTOR_NAME, null);

    private Long code;
    private boolean active = true;
    private String email;
    private String phone;
    private String title;
    private String department;
    private boolean taskEmailNotificationsEnabled = true;
    private boolean chatEmailNotificationsEnabled = true;

    protected Actor() {
    }

    public Actor(String name, String description, String fullName, Long code, String email, String phone, String title, String department) {
        super(name, description, fullName);
        setCode(code);
        setEmail(email != null ? email : "");
        setPhone(phone != null ? phone : "");
        setTitle(title != null ? title : "");
        setDepartment(department != null ? department : "");
    }

    public Actor(String name, String description, String fullName, Long code) {
        this(name, description, fullName, code, null, null, null, null);
    }

    public Actor(String name, String description, String fullName) {
        this(name, description, fullName, null);
    }

    public Actor(String name, String description) {
        this(name, description, name);
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.EXECUTOR;
    }

    @Column(name = "CODE")
    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Column(name = "E_MAIL", length = 1024)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "PHONE", length = 1024)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "TITLE", length = 1024)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "DEPARTMENT", length = 1024)
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Transient
    @Override
    protected String getComparisonValue() {
        return getFullName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).add("code", getCode()).toString();
    }

    @Transient
    public String getLastName() {
        if (getFullName() != null) {
            String[] strings = getFullName().split(" ", -1);
            if (strings.length > 0) {
                return strings[0];
            }
        }
        return "";
    }

    @Transient
    public String getFirstName() {
        if (getFullName() != null) {
            String[] strings = getFullName().split(" ", -1);
            if (strings.length > 1) {
                return strings[1];
            }
        }
        return "";
    }

    @Transient
    public String getMiddleName() {
        if (getFullName() != null) {
            String[] strings = getFullName().split(" ", -1);
            if (strings.length > 2) {
                return strings[2];
            }
        }
        return "";
    }

    @Column(name = "TASK_EMAIL_NOTIFICATIONS")
    public boolean getTaskEmailNotificationsEnabled() {
        return taskEmailNotificationsEnabled;
    }

    public void setTaskEmailNotificationsEnabled(boolean taskEmailNotificationsEnabled) {
        this.taskEmailNotificationsEnabled = taskEmailNotificationsEnabled;
    }

    @Column(name = "CHAT_EMAIL_NOTIFICATIONS")
    public boolean getChatEmailNotificationsEnabled() {
        return chatEmailNotificationsEnabled;
    }

    public void setChatEmailNotificationsEnabled(boolean chatEmailNotificationsEnabled) {
        this.chatEmailNotificationsEnabled = chatEmailNotificationsEnabled;
    }
}
