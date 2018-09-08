package ru.runa.wfe.commons.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import ru.runa.wfe.security.Permission;

public class Permission2XmlAdapter extends XmlAdapter<String, Permission> {

    @Override
    public Permission unmarshal(String v) throws Exception {
        return Permission.valueOf(v);
    }

    @Override
    public String marshal(Permission v) throws Exception {
        return v.getName();
    }
}
