package ru.runa.wfe.definition;

import ru.runa.wfe.commons.CalendarUtil;

import java.util.Calendar;


public class ProcessDefinitionChange{
    private Long version;
    private Long deploymentId;
    private Calendar date;
    private String author;
    private String comment;

    public ProcessDefinitionChange(){
        this.version = new Long(-1);
        this.deploymentId = new Long(-1);
        this.date = Calendar.getInstance();
        this.author = "";
        this.comment = "";
    }
    public ProcessDefinitionChange(Deployment deployment){
        this.version = deployment.getVersion();
        this.deploymentId = deployment.getId();
        this.date = CalendarUtil.dateToCalendar(deployment.getVersionDate());
        this.author = deployment.getVersionAuthor();
        this.comment = deployment.getVersionComment();
    }
    public ProcessDefinitionChange(Long version, Long deploymentId, Calendar date, String author, String comment){
        this.version = version;
        this.deploymentId = deploymentId;
        this.date = date;
        this.author = author;
        this.comment = comment;
    }
    public Long getVersion(){
        return this.version;
    }
    public void setVersion(Long version){
        this.version = version;
    }
    public Long getDeploymentId(){
        return this.deploymentId;
    }
    public void setDeploymentId(Long deploymentId){
        this.deploymentId = deploymentId;
    }
    public void setDate(Calendar date){
        this.date = date;
    }
    public Calendar getDate(){
        return this.date;
    }
    public String getAuthor(){
        return this.author;
    }
    public void setAuthor(String author){
        this.author = author;
    }
    public void setComment(String comment){
        this.comment = comment;
    }
    public String getComment(){
        return this.comment;
    }
}
