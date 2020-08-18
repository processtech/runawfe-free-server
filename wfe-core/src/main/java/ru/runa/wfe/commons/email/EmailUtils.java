package ru.runa.wfe.commons.email;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
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
import ru.runa.wfe.ConfigurationException;
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
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;

public class EmailUtils {
    private static final Log log = LogFactory.getLog(EmailConfig.class);
    private static MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    /**
     * Sends email in non-blocking and transactional mode
     */
    public static void sendMessageRequest(EmailConfig config) {
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

    public static void prepareMessage(User user, EmailConfig config, Interaction interaction, VariableProvider variableProvider) {
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
            FileVariable fileVariable = variableProvider.getValue(FileVariable.class, variableName);
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
            ExecutorDao executorDao = ApplicationContextFactory.getExecutorDAO();
            Collection<Actor> actors = executorDao.getGroupActors((Group) executor);
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

    public static boolean isProcessNameMatching(String processName, ProcessNameFilter includeFilter, ProcessNameFilter excludeFilter) {
        if (includeFilter != null && !includeFilter.isMatching(processName)) {
            return false;
        }
        if (excludeFilter != null && excludeFilter.isMatching(processName)) {
            return false;
        }
        return true;
    }

    public static List<String> filterEmails(final List<String> emailsToSend, EmailsFilter includeFilter, EmailsFilter excludeFilter) {
        List<String> filteredEmailsToSend = new LinkedList<>(emailsToSend);
        for (Iterator<String> i = filteredEmailsToSend.iterator(); i.hasNext(); ) {
            String email = i.next();
            if ((includeFilter != null) && (!includeFilter.isMatching(email))) {
                i.remove();
                continue;
            }

            if ((excludeFilter != null) && excludeFilter.isMatching(email)) {
                i.remove();
            }
        }
        return new ArrayList<>(filteredEmailsToSend);
    }

    /**
     * Validates and creates e-mail filter object
     *
     * @param pattern
     * @return filter object
     */
    public static EmailsFilter validateAndCreateEmailsFilter(final List<String> filters) {
        return EmailsFilter.create(filters);
    }

    public static ProcessNameFilter validateAndCreateProcessNameFilter(final List<String> filters) {
        return ProcessNameFilter.create(filters);
    }

    public static SwimlaneNameFilter validateAndCreateSwimlaneNameFilter(final List<String> filters) {
        return SwimlaneNameFilter.create(filters);
    }

    public static boolean isSwimlaneNameMatching(String swimlaneName, SwimlaneNameFilter includeSwimlaneNameFiler, SwimlaneNameFilter excludeSwimlaneNameFiler) {
        if (includeSwimlaneNameFiler != null && !includeSwimlaneNameFiler.isMatching(swimlaneName)) {
            return false;
        }
        if (excludeSwimlaneNameFiler != null && excludeSwimlaneNameFiler.isMatching(swimlaneName)) {
            return false;
        }
        return true;
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

    private static abstract class RegexFilter {
        private final List<String> filters;
        private final List<Pattern> patterns;

        private RegexFilter(final List<String> filters) {
            this.filters = filters;
            this.patterns = new ArrayList<>(filters.size());
            for (String filter : filters) {
                patterns.add(Pattern.compile(filter, Pattern.CASE_INSENSITIVE));
            }
        }

        boolean isMatching(String input) {
            for (Pattern p : patterns) {
                if (p.matcher(input).matches()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return filters.toString();
        }
    }

    private static abstract class WildcardFilter extends RegexFilter {

        private final List<String> filters;

        private WildcardFilter(final List<String> filters) {
            super(filtersToRegex(filters));
            this.filters = filters;
        }

        private static List<String> filtersToRegex(final List<String> filters) {
            List<String> results = new ArrayList<>(filters.size());
            for (String filter : filters) {
                results.add(filterToRegex(filter));
            }
            return results;
        }

        private static String filterToRegex(String filter) {
            return filter.replace(".", "\\.").replace("*", ".*").replace('?', '.');
        }

        @Override
        public String toString() {
            return filters.toString();
        }
    }

    public static final class ProcessNameFilter extends RegexFilter {

        private ProcessNameFilter(final List<String> filters) {
            super(filters);
        }

        private static ProcessNameFilter create(List<String> filters) {
            return new ProcessNameFilter(filters);
        }
    }

    public static final class SwimlaneNameFilter extends RegexFilter {

        private SwimlaneNameFilter(final List<String> filters) {
            super(filters);
        }

        private static SwimlaneNameFilter create(List<String> filters) {
            return new SwimlaneNameFilter(filters);
        }
    }

    public static final class EmailsFilter extends WildcardFilter {

        private EmailsFilter(final List<String> filters) {
            super(filters);
        }

        private static EmailsFilter create(List<String> filters) {
            for (String filter : filters) {
                filter = filter.trim();
                if (!isEmailsFilterValid(filter)) {
                    throw new ConfigurationException("Incorrect email filter pattern: " + filter);
                }
            }
            return new EmailsFilter(filters);
        }

        static boolean isEmailsFilterValid(String f) {
            int atCount = 0;
            for (int i = 0; i < f.length(); i++) {
                char ch = f.charAt(i);
                if (((ch >= 'a') && (ch <= 'z')) || ((ch >= '0') && (ch <= '9')) || (ch == '_') || (ch == '.')) {
                    continue;
                }
                if (ch == '?' || ch == '*') {
                    continue;
                }
                if (atCount == 0 && ch == '@') {
                    atCount++;
                    continue;
                }
                return false;
            }
            if (atCount == 0) {
                return false;
            }
            final String[] parts = f.split("@", 2);
            return !(parts[0].isEmpty() || parts[1].isEmpty());
        }
    }

}
