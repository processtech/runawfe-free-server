package ru.runa.wfe.definition;

import java.util.Calendar;

public class ProcessDefinitionChange{
    private Long version;
    private Calendar date;
    private String author;
    private String comment;

    public ProcessDefinitionChange(){
        this.version = new Long(-1);
        this.date = Calendar.getInstance();
        this.author = "";
        this.comment = "";
    }
    public ProcessDefinitionChange(long version, VersionInfo versionInfo){
        this.version = version;
        this.date = versionInfo.getDate();
        this.author = versionInfo.getAuthor();
        this.comment = versionInfo.getComment();
    }
    public Long getVersion(){
        return this.version;
    }
    public void setVersion(Long version){
        this.version = version;
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
