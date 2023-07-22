package ru.runa.bp.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.runa.alfresco.AlfObject;
import ru.runa.alfresco.AlfTypeDesc;
import ru.runa.alfresco.anno.Property;
import ru.runa.alfresco.anno.Type;

@Type(prefix = "demo", name = "mydoc")
public class MyDoc extends AlfObject {
    private static final long serialVersionUID = 1L;
    public static final String STATUS_NEW = "CREATED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @Property(name = "mydocstatus")
    private String status = STATUS_NEW;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    protected String getNewObjectName(AlfTypeDesc typeDesc) {
        return "MyDoc-" + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date());
    }

}
