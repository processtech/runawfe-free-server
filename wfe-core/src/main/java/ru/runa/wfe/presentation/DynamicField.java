package ru.runa.wfe.presentation;

import java.io.Serializable;

import com.google.common.base.Objects;

class DynamicField implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Index against classPresentation.getFields();
     */
    private Long fieldIdx;
    /**
     * Value inserted by user (variable name for example)
     */
    private String fieldValue;

    public DynamicField() {
    }

    public DynamicField(long fieldIdx, String fieldValue) {
        this.fieldIdx = fieldIdx;
        this.fieldValue = fieldValue;
    }

    public Long getFieldIdx() {
        return fieldIdx;
    }

    public void setFieldIdx(Long fieldIdx) {
        this.fieldIdx = fieldIdx;
    }

    public String getDynamicValue() {
        return fieldValue;
    }

    public void setDynamicValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldIdx, fieldValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DynamicField)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DynamicField other = (DynamicField) obj;
        return Objects.equal(fieldIdx, other.fieldIdx) && Objects.equal(fieldValue, other.fieldValue);
    }

    @Override
    public String toString() {
        return fieldIdx + "=" + fieldValue;
    }
}
