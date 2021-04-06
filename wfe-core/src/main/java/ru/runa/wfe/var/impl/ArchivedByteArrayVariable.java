package ru.runa.wfe.var.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import ru.runa.wfe.var.ArchivedVariable;

@Entity
@DiscriminatorValue(value = "B")
public class ArchivedByteArrayVariable extends ArchivedVariable<byte[]> {
    private byte[] object;

    @Override
    @Lob
    @Column(length = 16777216, name = "BYTES")
    public byte[] getStorableValue() {
        return object;
    }

    @Override
    protected void setStorableValue(byte[] object) {
        this.object = object;
    }

    @Override
    public boolean supports(Object value) {
        // ATTENTION! Same logic in CurrentByteArrayVariable.
        return super.supports(value) || value instanceof byte[];
    }
}
