package ru.runa.wfe.extension.handler;

import java.io.StringReader;

import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.execution.ExecutionContext;

import ru.runa.wfe.redmine.api.RedmineFacade;
import ru.runa.wfe.redmine.api.RedmineFacadeFactory;
import ru.runa.wfe.redmine.api.ActivityType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import com.taskadapter.redmineapi.bean.TimeEntry;

/**
 * Handler for write to redmine user time entry.
 */
public class RedmineTimeEntryActionHandler extends ActionHandlerBase {

    @XmlRootElement
    public static class Config {
        private int           issueId;
        private String        userName;
        private String        activityType;
        private String        comment;
        private float         hours;

        public int getIssueId() {
            return this.issueId;
        }

        @XmlElement
        public void setIssueId(int issueId) {
            this.issueId = issueId;
        }

        public String getUserName() {
            return this.userName;
        }

        @XmlElement
        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getComment() {
            return this.comment;
        }

        @XmlElement
        public void setComment(String comment) {
            this.comment = comment;
        }

        public float getHours() {
            return this.hours;
        }

        @XmlElement
        public void setHours(float hours) {
            this.hours = hours;
        }

        public String getActivityType() {
            return this.activityType;
        }

        @XmlElement
        public void setActivityType(String type) {
            this.activityType = type;
        }
    }


    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        RedmineFacade facade            = null;
        JAXBContext   jaxbContext       = null;
        Unmarshaller  jaxbUnmarshaller  = null;
        Config        config            = null;
        Object        obj               = null;
        TimeEntry     timeEntry         = null;

        if ((this.configuration != null) &&
             ((facade = RedmineFacadeFactory.create()) != null) &&
             ((jaxbContext = JAXBContext.newInstance(
                  Config.class)) != null) &&
             ((jaxbUnmarshaller = jaxbContext.createUnmarshaller()) != null) &&
             ((obj = jaxbUnmarshaller.unmarshal(
                  new StringReader(
                      this.configuration))) != null) &&
             (obj instanceof Config) &&
             ((config = (Config)obj) != null) &&
             ((timeEntry = facade.addTimeEntry(
                  config.getIssueId(), 
                  config.getUserName(), 
                  ActivityType.valueOf(
                      config.getActivityType()), 
                  config.getComment(),
                  config.getHours())) == null)) {
            throw new Exception("Issue did not found");
        }
        else if (this.configuration == null) {
            throw new Exception("Invalid configuration. configuration should be not null");
        }
        else if (facade == null) {
            throw new Exception("Invalid redmine configuration.");
        }
        else if ((jaxbContext == null) || (jaxbUnmarshaller == null)) {
            throw new Exception("Invalid JAXB configuration.");
        }
        else if ((obj == null) || (config == null)) {
            throw new Exception(
                "Invalid configuration format. configuration should be match" +
                "<config><issueId>Integer</issueId><userName>String</userName>" + 
                "<comment>String</comment><hours>Float</hours>" + 
                "<activityType>[DEVELOP|DESIGN|DESIGN|PM|REQUIREMENTS]</activityType></config>");
        }
	}
}