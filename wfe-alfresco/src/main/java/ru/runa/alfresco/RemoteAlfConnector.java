package ru.runa.alfresco;

import org.alfresco.webservice.authentication.AuthenticationFault;
import org.alfresco.webservice.util.AuthenticationUtils;
import org.alfresco.webservice.util.WebServiceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Authenticated wrapper for {@link RemoteAlfConnection}.
 * 
 * @author dofs
 */
public abstract class RemoteAlfConnector<T> {
    protected static Log log = LogFactory.getLog(RemoteAlfConnector.class);
    protected final RemoteAlfConnection alfConnection = new RemoteAlfConnection();
    private static int sessionIdCounter = 0;
    private static ThreadLocal<SessionData> sessions = new ThreadLocal<SessionData>();

    static {
        WebServiceFactory.setEndpointAddress(WSConnectionSettings.getInstance().getEndpointAddress());
        WebServiceFactory.setTimeoutMilliseconds(7 * 60000);
        System.out.println("Using " + WSConnectionSettings.getInstance().getEndpointAddress());
    }

    protected static void log(String message) {
        log.debug(sessions.get().id + "(" + sessions.get().level + "): " + message);
    }

    public static void sessionStart() throws AuthenticationFault {
        if (sessions.get() == null) {
            AuthenticationUtils.startSession(WSConnectionSettings.getInstance().getLogin(), WSConnectionSettings.getInstance().getPassword());
            sessions.set(new SessionData());
            log("Started new alfConnection");
        }
        sessions.get().level++;
    }

    public static void sessionEnd() {
        if (sessions.get() != null) {
            sessions.get().level--;
            if (sessions.get().level == 0) {
                log("Ending alfConnection");
                AuthenticationUtils.endSession();
                sessions.remove();
            }
        }
    }

    public final T runInSession() {
        try {
            sessionStart();
            return code();
        } catch (Exception e) {
            if (ConnectionException.MESSAGE.equals(e.getMessage())) {
                throw new ConnectionException();
            }
            throw RemoteAlfConnection.propagate(e);
        } finally {
            sessionEnd();
        }
    }

    protected abstract T code() throws Exception;

    static class SessionData {
        final int id = sessionIdCounter++;
        int level = 0;
    }
}
