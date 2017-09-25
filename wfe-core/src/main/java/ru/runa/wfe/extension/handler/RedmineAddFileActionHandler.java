package ru.runa.wfe.extension.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;

import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.execution.ExecutionContext;

import ru.runa.wfe.redmine.api.RedmineFacade;
import ru.runa.wfe.redmine.api.RedmineFacadeFactory;
import ru.runa.wfe.redmine.api.ActivityType;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.taskadapter.redmineapi.bean.TimeEntry;

/**
 * Handler for append files to issue of redmine.
 */
public class RedmineAddFileActionHandler extends ActionHandlerBase {

    public static class FileInfo {
        @XmlAttribute
        public String name;
        @XmlValue
        public String value;
    }

    @XmlRootElement(name="files")
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Files {
        private FileInfo[] arr;
 
        @XmlElement(name = "file")
        public FileInfo[] getArray() {
            return arr;
        }
     
        public void setArray(FileInfo[] arr) {
            this.arr = arr;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.NONE)
    public static class Config {
        private int                  issueId;
        private String               userName;
        private Files 				 files;

        public Config() {
        }

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

        @XmlElement
        public void setFiles(Files files) {
            this.files = files;
        }

        public Files getFiles() {
            return this.files;
        }

        public Map<String, InputStream> getFileMap() {
            Map<String, InputStream> map = new HashMap<String, InputStream>();
            FileInfo[]               arr = this.files.getArray();

            for (FileInfo file : arr) {
                map.put(
                    file.name,
                    new ByteArrayInputStream(
                        DatatypeConverter.parseBase64Binary(
                            file.value)));
            }

            return map;
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
             ((config = (Config)obj) != null)) {
            facade.updateIssue(
                config.getIssueId(),
                config.getUserName(),
                config.getFileMap());
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
                "<files><file name=\"File name\">File content base64</file></files></config>");
        }
	}
}