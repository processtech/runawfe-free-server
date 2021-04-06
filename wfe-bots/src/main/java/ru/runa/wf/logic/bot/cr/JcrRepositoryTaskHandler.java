package ru.runa.wf.logic.bot.cr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;
import javax.naming.InitialContext;

import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariableImpl;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

public class JcrRepositoryTaskHandler extends TaskHandlerBase {
    private static final String NT_FILE = "nt:file";
    private static final String NT_RESOURCE = "nt:resource";
    private static final String JCR_ENCODING = "jcr:encoding";
    private static final String JCR_DATA = "jcr:data";
    private static final String JCR_MIME_TYPE = "jcr:mimeType";
    private static final String JCR_LAST_MODIFIED = "jcr:lastModified";
    private static final String JCR_CONTENT = "jcr:content";

    private JcrTaskConfig config;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        config = ConfigXmlParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        Session session = null;
        try {
            Context context = new InitialContext();
            Repository repository = (Repository) context.lookup(config.getRepositoryName());
            Credentials credentials = new SimpleCredentials(config.getUserName(), config.getPassword().toCharArray());
            session = repository.login(credentials);

            Map<String, Object> outputVariables = new HashMap<String, Object>();

            for (JcrTask jcrTask : config.getTasks()) {
                if (JcrTask.GET_FILE.equals(jcrTask.getOperationName())) {
                    FileVariableImpl fileVariable = getFile(session, jcrTask.getPath(), jcrTask.getFileName());
                    outputVariables.put(jcrTask.getVariableName(), fileVariable);
                }
                if (JcrTask.PUT_FILE.equals(jcrTask.getOperationName())) {
                    FileVariableImpl fileVariable = variableProvider.getValue(FileVariableImpl.class, jcrTask.getVariableName());
                    putFile(session, fileVariable, jcrTask.getPath(), jcrTask.getFileName());
                }
                if (JcrTask.REMOVE_FILE.equals(jcrTask.getOperationName())) {
                    deleteFile(session, jcrTask.getPath(), jcrTask.getFileName());
                }
            }
            return outputVariables;
        } finally {
            if (session != null) {
                session.save();
                session.logout();
            }
        }
    }

    private void putFile(Session session, FileVariableImpl fileVariable, String path, String fileName) throws RepositoryException {
        if (!session.getRootNode().hasNode(path)) {
            session.getRootNode().addNode(path);
        }
        Node folderNode = session.getRootNode().getNode(path);
        Node contentNode;
        if (!folderNode.hasNode(fileName)) {
            // create the file node - see section 6.7.22.6 of the spec
            Node fileNode = folderNode.addNode(fileName, NT_FILE);
            // create the mandatory child node - jcr:content
            contentNode = fileNode.addNode(JCR_CONTENT, NT_RESOURCE);
            contentNode.setProperty(JCR_MIME_TYPE, fileVariable.getContentType());
            contentNode.setProperty(JCR_ENCODING, Charsets.UTF_8.name());
        } else {
            Node fileNode = folderNode.getNode(fileName);
            contentNode = fileNode.getNode(JCR_CONTENT);
        }
        contentNode.setProperty(JCR_DATA, new ByteArrayInputStream(fileVariable.getData()));
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTimeInMillis(System.currentTimeMillis());
        contentNode.setProperty(JCR_LAST_MODIFIED, lastModified);
    }

    public FileVariableImpl getFile(Session session, String path, String fileName) throws RepositoryException, IOException {
        Node folderNode = session.getRootNode().getNode(path);
        Node fileNode = folderNode.getNode(fileName);
        Node file = fileNode.getNode(JCR_CONTENT);
        String contentType = file.getProperty(JCR_MIME_TYPE).getString();
        Property dataProperty = file.getProperty(JCR_DATA);
        InputStream stream = null;
        try {
            stream = dataProperty.getBinary().getStream();
            byte[] content = ByteStreams.toByteArray(stream);
            return new FileVariableImpl(fileNode.getName(), content, contentType);
        } finally {
            Closeables.closeQuietly(stream);
        }
    }

    public void deleteFile(Session session, String path, String fileName) throws RepositoryException, IOException {
        Node folderNode = session.getRootNode().getNode(path);
        if (folderNode.hasNode(fileName)) {
            folderNode.getNode(fileName).remove();
        }
    }
}
