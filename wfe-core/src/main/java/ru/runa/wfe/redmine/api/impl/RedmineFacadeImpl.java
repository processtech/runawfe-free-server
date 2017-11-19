package ru.runa.wfe.redmine.api.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.taskadapter.redmineapi.AttachmentManager;
import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.TimeEntryManager;
import com.taskadapter.redmineapi.UserManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.TimeEntry;
import com.taskadapter.redmineapi.bean.TimeEntryFactory;
import com.taskadapter.redmineapi.bean.User;

import ru.runa.wfe.redmine.api.ActivityMap;
import ru.runa.wfe.redmine.api.ActivityType;
import ru.runa.wfe.redmine.api.RedmineConfig;
import ru.runa.wfe.redmine.api.RedmineFacade;

/**
 * Реализация интерфейса для работы с redmine
 * @author Veniamin
 *
 */
public class RedmineFacadeImpl implements RedmineFacade {
    
    private static final String redmineConfig = "redmineConfig";
    
    private ClassPathXmlApplicationContext ctx;
    private String contextName;
    
    public RedmineFacadeImpl(String contextName) {        
        this.ctx = null;
        this.contextName = contextName;
    }
    
    private RedmineManager getRedmineManager() {
        RedmineManager          res    = null;
        Object                  obj    = null;
        RedmineConfig           config = null;
        
        if (this.ctx == null) {
            this.ctx = new ClassPathXmlApplicationContext(
                    this.contextName);
        }
        
        if (((obj = this.ctx.getBean(
                RedmineFacadeImpl.redmineConfig)) != null) &&
            (obj instanceof RedmineConfigImpl) &&
            ((config = (RedmineConfig)obj) != null)) {
            res = config.getRedmineManager();
        }
        
        return res;
    }
    
    private List<User> getUsers() 
            throws RedmineException {
        List<User>                res                = null;
        RedmineManager            redmineManager     = null;
        UserManager                userManager       = null;
        
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            userManager = redmineManager.getUserManager();
            
            res = userManager.getUsers();
        }
        
        return res;
    }
    
    private User findUserByLogin(
        String login) 
            throws RedmineException {
        User                      res  = null;
        List<User>                list = null;
        
        Objects.requireNonNull(login);
        
        list = this.getUsers();
        
        if (list != null) {
            for (User user : list) {
                if ((user != null) &&
                    login.equals(
                        user.getLogin())) {
                    res = user;
                    break;
                }
            }
        }
        
        return res;
    }
    
    public TimeEntry addTimeEntry(
            int issueId, 
            String userName,
            ActivityType activityType,
            String comment,
            float hours) 
                throws Exception {
        TimeEntry                  res               = null;
        RedmineManager             redmineManager    = null;
        TimeEntryManager           timeEntryManager  = null;
        TimeEntry                  entry             = null;
        int                        activityId        = 0;
        
        Objects.requireNonNull(userName);
        Objects.requireNonNull(activityType);
        activityId = ActivityMap.getType(activityType);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            timeEntryManager = redmineManager.getTimeEntryManager();
            entry = TimeEntryFactory.create();
            
            entry.setUserName(userName);
            entry.setHours(hours);
            entry.setIssueId(issueId);
            entry.setActivityId(activityId);
            entry.setComment(comment);
            
            res = timeEntryManager.createTimeEntry(entry);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
        
        return res;
    }
    
    public void removeTimeEntry(
        int id) 
            throws Exception {
        RedmineManager            redmineManager    = null;
        TimeEntryManager          timeEntryManager  = null;
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            timeEntryManager = redmineManager.getTimeEntryManager();
            
            timeEntryManager.deleteTimeEntry(id);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }

    public Project createProject(
            String name,
            String id) 
                throws Exception {
        Project               res               = null;
        RedmineManager        redmineManager    = null;
        ProjectManager        projectManager    = null;
        Project               tmpProject        = null;
        
        Objects.requireNonNull(name);
        Objects.requireNonNull(id);
        
        tmpProject = ProjectFactory.create(name, id);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            projectManager = redmineManager.getProjectManager();
            
            res = projectManager.createProject(tmpProject);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
        
        return res;
    }

    public void removeProject(
            String id) 
                throws Exception {
        RedmineManager        redmineManager    = null;
        ProjectManager        projectManager    = null;
        
        Objects.requireNonNull(id);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            projectManager = redmineManager.getProjectManager();
            
            projectManager.deleteProject(id);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }

    public Issue createIssue(
            int projectId, 
            Date startDate, 
            Date dueDate, 
            String assignetName, 
            String subject,
            String description,
            float estimateHours) 
                throws Exception {
        Issue                 res               = null;
        Issue                 issue             = null;
        RedmineManager          redmineManager  = null;
        IssueManager          issueManager      = null;
        User                  user              = null;
        
        Objects.requireNonNull(startDate);
        Objects.requireNonNull(dueDate);
        Objects.requireNonNull(subject);
        Objects.requireNonNull(description);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            issueManager = redmineManager.getIssueManager();
            
            if ((user = this.findUserByLogin(
                    assignetName)) == null) {
                throw new IllegalStateException(
                    "user did not found");
            }
            
            issue = IssueFactory.create(projectId, subject);
            
            issue.setStartDate(startDate);
            issue.setDueDate(dueDate);
            issue.setAssigneeId(user.getId());
            issue.setDescription(description);
            issue.setEstimatedHours(estimateHours);
            
            res = issueManager.createIssue(issue);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
        
        return res;
    }

    public void removeIssue(
        int id) 
            throws Exception {
        RedmineManager        redmineManager    = null;
        IssueManager          issueManager      = null;
        
        Objects.requireNonNull(id);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            issueManager = redmineManager.getIssueManager();
            
            issueManager.deleteIssue(id);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }

    public void updateIssue(
        int issueId, 
        float estimateHours) 
            throws Exception {
        Issue                 issue             = null;
        RedmineManager        redmineManager    = null;
        IssueManager          issueManager      = null;
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            issueManager = redmineManager.getIssueManager();
            
            issue = issueManager.getIssueById(
                issueId, 
                Include.attachments);
            
            issue.setEstimatedHours(estimateHours);

            issueManager.update(issue);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }

    public void updateIssue(
        int issueId, 
        float estimateHours, 
        String assigneeName) 
            throws Exception {
        Issue                 issue             = null;
        RedmineManager        redmineManager    = null;
        IssueManager          issueManager      = null;
        User                  user              = null;
        
        Objects.requireNonNull(assigneeName);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            issueManager = redmineManager.getIssueManager();
            
            issue = issueManager.getIssueById(
                issueId, 
                Include.attachments);
            
            if ((user = this.findUserByLogin(
                    assigneeName)) == null) {
                throw new IllegalStateException(
                    "user did not found");
            }
            
            issue.setEstimatedHours(estimateHours);
            issue.setAssigneeId(user.getId());

            issueManager.update(issue);
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }
    
    private void remove(
        Collection<Attachment> colect, 
        AttachmentManager attachmentManager) 
            throws RedmineException {
        RedmineException    tmp = null;
        
        Objects.requireNonNull(attachmentManager);
        Objects.requireNonNull(colect);
        
        for (Attachment attachment : colect) {
            if (attachment != null) {
                try {
                    attachmentManager.delete(
                        attachment.getId());
                }
                
                catch (RedmineException exc) {
                    tmp = exc;
                }
            }
        }
        
        if (tmp != null)
            throw tmp;
    }
    
    private Pair<InputStream, Long> convert(
            InputStream is) 
                throws IOException {
        Pair<InputStream, Long>        res = null;
        ByteArrayOutputStream        bos = null;
        int                            sim = 0;
        long                        len = 0;
        
        Objects.requireNonNull(is);
        
        try
        {
            bos = new ByteArrayOutputStream();
            
            while ((sim = is.read()) !=-1) {
                bos.write(sim);
                len++;
            }
        
            res = new ImmutablePair<InputStream, Long>(
                    new ByteArrayInputStream(
                        bos.toByteArray()), 
                    len);
        }
        
        finally {
            if (bos != null)
                bos.close();
        }
        
        return res;
    }
    
    private Collection<Attachment> upload(
        Map<String, InputStream> files,
        AttachmentManager attachmentManager) 
            throws RedmineException, IOException {
        Collection<Attachment>             res         = null;
        Set<Entry<String, InputStream>>    set         = null;
        String                             fileName    = null;
        InputStream                        fileContent = null;
        Attachment                         attachment  = null;
        Pair<InputStream, Long>            pair        = null;
        
        try {
            res = new ArrayList<Attachment>();
            set = files.entrySet();
            
            for (Entry<String, InputStream> entry : set) {
                if ((entry != null) &&
                    ((fileName = entry.getKey()) != null) &&
                    ((fileContent = entry.getValue()) != null) &&
                    ((pair = this.convert(
                        fileContent)) != null)) {
                    
                    if ((attachment = attachmentManager.uploadAttachment(
                            fileName, 
                            "application/ternary", 
                            pair.getLeft(),
                            pair.getRight())) != null) {
                        res.add(attachment);
                    }
                    else {
                        this.remove(
                            res, 
                            attachmentManager);
                        
                        res.clear();
                        break;
                    }
                }
            }
        }
        
        catch (RedmineException exc) {
            if (res != null) {
                this.remove(
                    res, 
                    attachmentManager);
                
                res.clear();
            }
            
            throw exc; 
        }
        
        catch (IOException exc) {
            if (res != null) {
                this.remove(
                    res,
                    attachmentManager);
                
                res.clear();
            }
            
            throw exc; 
        }
        
        return res;
    }

    public void updateIssue(
        int issueId, 
        String userName, 
        Map<String, InputStream> files) 
            throws Exception {
        RedmineManager                  redmineManager    = null;
        AttachmentManager               attachmentManager = null;
        IssueManager                    issueManager      = null;
        Issue                           issue             = null;
        Collection<Attachment>          collect           = null;
        User                            user              = null;
        
        Objects.requireNonNull(userName);
        Objects.requireNonNull(files);
        
        if ((redmineManager = this.getRedmineManager()) != null) {
            attachmentManager = redmineManager.getAttachmentManager();
            issueManager = redmineManager.getIssueManager();
            
            if ((issue = issueManager.getIssueById(
                    issueId, 
                    Include.attachments)) != null) {
                
                if ((user = this.findUserByLogin(
                        userName)) == null) {
                    throw new IllegalStateException(
                        "user did not found");
                }
                
                issue.setAssigneeId(user.getId());
                
                collect = this.upload(
                    files, 
                    attachmentManager);
                
                if ((collect != null) &&
                    (collect.size() > 0)) {
                    issue.addAttachments(
                        collect);
                
                    issueManager.update(
                        issue);
                }
            }
            else
            {
                throw new IllegalStateException(
                    "issue did not found");
            }
        }
        else {
            throw new IllegalStateException(
                "redmine manager is null");
        }
    }
}
