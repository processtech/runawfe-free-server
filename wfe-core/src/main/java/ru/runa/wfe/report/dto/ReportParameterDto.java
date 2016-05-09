package ru.runa.wfe.report.dto;

import ru.runa.wfe.report.ReportParameterType;

public class ReportParameterDto {
    private String userName;
    private String description;
    private String internalName;
    private int position;
    private ReportParameterType type;
    private boolean isRequired;

    public ReportParameterDto() {
        super();
    }

    public ReportParameterDto(String userName, String description, String internalName, int position, ReportParameterType type, boolean isRequired) {
        super();
        this.userName = userName;
        this.description = description;
        this.internalName = internalName;
        this.position = position;
        this.type = type;
        this.isRequired = isRequired;
    }

    public String getUserName() {
        return userName == null ? "" : userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInternalName() {
        return internalName == null ? "" : internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ReportParameterType getType() {
        return type;
    }

    public void setType(ReportParameterType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((internalName == null) ? 0 : internalName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReportParameterDto other = (ReportParameterDto) obj;
        if (internalName == null) {
            if (other.internalName != null) {
                return false;
            }
        } else if (!internalName.equals(other.internalName)) {
            return false;
        }
        if (isRequired != other.isRequired) {
            return false;
        }
        if (position != other.position) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

}
