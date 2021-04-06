package ru.runa.wfe.commons.web;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import ru.runa.wfe.var.SelectableOption;

/**
 * Specified package for this class is essential due to back compatibility.
 * @author dofs
 */
public class Option implements SelectableOption, Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;
    private final String label;

    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return value + "(" + label + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Option) {
            return value.equals(((Option) obj).value);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    // http://lingpipe-blog.com/2009/08/10/serializing-immutable-singletons-serialization-proxy/
    private static class SerializationProxy implements Externalizable {
        private static final long serialVersionUID = -4163914145668867283L;
        String value;
        String text;

        @SuppressWarnings("unused")
        public SerializationProxy() {
        }

        public SerializationProxy(Option option) {
            value = option.value;
            text = option.label;
        }

        Object readResolve() {
            return new Option(value, text);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = (String) in.readObject();
            text = (String) in.readObject();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(value);
            out.writeObject(text);
        }
    }
}
