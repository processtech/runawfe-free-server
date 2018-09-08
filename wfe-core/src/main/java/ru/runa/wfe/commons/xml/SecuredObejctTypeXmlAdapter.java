package ru.runa.wfe.commons.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import ru.runa.wfe.security.SecuredObjectType;

public class SecuredObejctTypeXmlAdapter extends XmlAdapter<String, SecuredObjectType> {

    @Override
    public SecuredObjectType unmarshal(String v) throws Exception {
        return SecuredObjectType.valueOf(v);
    }

    @Override
    public String marshal(SecuredObjectType v) throws Exception {
        return v.getName();
    }
}
