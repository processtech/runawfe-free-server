package ru.runa.wfe.commons.email;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.email.EmailConfig.Attachment;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class EmailUtils {
    private static final Log log = LogFactory.getLog(EmailConfig.class);
    private static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
    static {
        System.setProperty("mail.mime.encodefilename", "true");
    }

    /**
     * Sends email in non-blocking and transactional mode
     */
    public static void sendMessageRequest(EmailConfig config) throws Exception {
        config.checkValid();
        if (Strings.isNullOrEmpty(config.getHeaderProperties().get(EmailConfig.HEADER_TO))) {
            log.warn("Ignored message with empty 'To' recipients: " + config);
            return;
        }
        Utils.sendEmailRequest(config);
    }

    /**
     * Sends email immediately
     */
    public static void sendMessage(EmailConfig config) throws Exception {
        if (config.getMessageId() != null) {
            log.info("Sending " + config.getMessageId());
        }
        config.checkValid();
        if (Strings.isNullOrEmpty(config.getHeaderProperties().get(EmailConfig.HEADER_TO))) {
            log.warn("Ignored message with empty 'To' recipients: " + config);
            return;
        }
        Properties props = new Properties();
        props.putAll(config.getConnectionProperties());
        String protocol = props.getProperty(EmailConfig.CONNECTION_MAIL_TRANSPORT);
        String connectionTimepoutPropName = "mail." + protocol + ".connectiontimeout";
        if (!props.contains(connectionTimepoutPropName)) {
            props.put(connectionTimepoutPropName, SystemProperties.getEmailDefaultTimeoutInMilliseconds());
        }
        String timepoutPropName = "mail." + protocol + ".timeout";
        if (!props.contains(timepoutPropName)) {
            props.put(timepoutPropName, SystemProperties.getEmailDefaultTimeoutInMilliseconds());
        }

        if (config.getHeaderProperties().containsKey("Subject")) {
            String subject = config.getHeaderProperties().get("Subject");
            subject = MimeUtility.encodeText(subject, Charsets.UTF_8.name(), null);
            config.getHeaderProperties().put("Subject", subject);
        }

        PasswordAuthenticator authenticator = null;
        boolean auth = "true".equals(props.getProperty("mail." + protocol + ".auth"));
        if (auth) {
            String username = props.getProperty(EmailConfig.CONNECTION_MAIL_USER);
            String password = props.getProperty(EmailConfig.CONNECTION_MAIL_PASSWORD);
            Preconditions.checkNotNull(username, "Authenticaton enabled but property " + EmailConfig.CONNECTION_MAIL_USER + " is not set");
            Preconditions.checkNotNull(password, "Authenticaton enabled but property " + EmailConfig.CONNECTION_MAIL_PASSWORD + " is not set");
            authenticator = new PasswordAuthenticator(username, password);
            if (!config.getHeaderProperties().containsKey("From")) {
                config.getHeaderProperties().put("From", username);
            }
        }

        Session session = Session.getInstance(props, authenticator);
        MimeMessage msg = new MimeMessage(session);
        for (String headerName : config.getHeaderProperties().keySet()) {
            String headerValue = config.getHeaderProperties().get(headerName);
            msg.setHeader(headerName, headerValue);
        }
        Multipart multipart = new MimeMultipart("related");
        MimeBodyPart part = new MimeBodyPart();
        part.setText(config.getMessage(), Charsets.UTF_8.name(), config.getMessageType());
        multipart.addBodyPart(part);
        for (Attachment attachment : config.getAttachments()) {
            MimeBodyPart attach = new MimeBodyPart();
            attach.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment.content, fileTypeMap.getContentType(attachment.fileName))));
            if (attachment.inlined) {
                attach.setHeader("Content-ID", attachment.fileName);
                attach.setDisposition(Part.INLINE);
            } else {
                attach.setFileName(attachment.fileName);
            }
            multipart.addBodyPart(attach);
        }
        msg.setContent(multipart);
        log.info("Connecting to [" + protocol + "]: " + props.getProperty(EmailConfig.CONNECTION_MAIL_HOST) + ":"
                + props.getProperty("mail." + protocol + ".port"));
        Transport transport = session.getTransport();
        try {
            transport.connect();
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
            String debugMessage = "Message sent";
            Address[] toAddresses = msg.getRecipients(RecipientType.TO);
            if (toAddresses != null) {
                debugMessage += " To:" + Arrays.asList(toAddresses);
            }
            Address[] ccAddresses = msg.getRecipients(RecipientType.CC);
            if (ccAddresses != null) {
                debugMessage += " Cc:" + Arrays.asList(ccAddresses);
            }
            log.info(debugMessage);
        } finally {
            transport.close();
        }
    }

    public static void prepareMessage(User user, EmailConfig config, Interaction interaction, IVariableProvider variableProvider) {
        config.setMessageId(variableProvider.getProcessId() + ": " + (interaction != null ? interaction.getName() : "no interaction"));
        config.applySubstitutions(variableProvider);
        String formTemplate;
        if (config.isUseMessageFromTaskForm()) {
            Preconditions.checkNotNull(interaction, "Interaction is null but property bodyInlined=true");
            if (interaction.hasForm()) {
                formTemplate = new String(interaction.getFormData(), Charsets.UTF_8);
                if (!"ftl".equals(interaction.getType())) {
                    throw new InternalApplicationException("Property bodyInlined=true is applicable only to free form layout form (ftl)");
                }
            } else {
                if (SystemProperties.isV3CompatibilityMode()) {
                    formTemplate = " ";
                } else {
                    throw new InternalApplicationException("Property bodyInlined=true but form does not exist");
                }
            }
        } else {
            formTemplate = config.getMessage();
        }
        String formMessage = ExpressionEvaluator.process(user, formTemplate, variableProvider, null);
        config.setMessage(formMessage);
        log.debug(formMessage);
        for (String variableName : config.getAttachmentVariableNames()) {
            IFileVariable fileVariable = variableProvider.getValue(IFileVariable.class, variableName);
            if (fileVariable != null) {
                Attachment attachment = new Attachment();
                attachment.fileName = fileVariable.getName();
                attachment.content = fileVariable.getData();
                config.getAttachments().add(attachment);
            }
        }
    }

    public static List<String> getEmails(Executor executor) {
        List<String> emails = Lists.newArrayList();
        if (executor instanceof Actor) {
            Actor actor = (Actor) executor;
            if (actor.isActive() && !Utils.isNullOrEmpty(actor.getEmail())) {
                emails.add(actor.getEmail().trim());
            }
        } else if (executor instanceof Group) {
            ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
            Collection<Actor> actors = executorDAO.getGroupActors((Group) executor);
            for (Actor actor : actors) {
                if (actor.isActive() && !Utils.isNullOrEmpty(actor.getEmail())) {
                    emails.add(actor.getEmail().trim());
                }
            }
        }
        return emails;
    }

    public static String concatenateEmails(Collection<String> emails) {
        return Joiner.on(", ").join(emails);
    }

    private static class PasswordAuthenticator extends Authenticator {
        private final String username;
        private final String password;

        public PasswordAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

}
